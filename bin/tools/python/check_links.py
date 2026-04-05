#!/usr/bin/env python3
"""
check_links.py

Script to check all links in documentation (supports Markdown / HTML / reStructuredText-like files)
Features:
  1) Verify internal links (relative/absolute paths) point to existing files (optionally check anchors/headings)
  2) Test external links for reachability (uses HTTP HEAD first, does not download content for speed)
Usage:
  python check_links.py --root . --paths docs README.md --ext md,markdown,html,rst --concurrency 20 --timeout 5

Output:
  - Lists broken links (and reasons) in the terminal
  - Returns non-zero status code if broken links are detected (for CI integration)
Dependencies:
  - requests
Optional optimization: Run in parallel in CI (--concurrency adjusts concurrency)
"""

import re
import os
import sys
import argparse
import logging
import threading
import fnmatch
from concurrent.futures import ThreadPoolExecutor, as_completed
from collections import defaultdict
from typing import List, Tuple, Dict, Set, Optional
import requests

# ---------- 配置 ----------
MARKDOWN_INLINE_LINK_RE = re.compile(r'(?<!\!)\[[^\]]*\]\(([^)]+)\)')  # Exclude image links starting with ![]
MARKDOWN_AUTOLINK_RE = re.compile(r'<(https?://[^ >]+)>')
MARKDOWN_REF_DEF_RE = re.compile(r'^\s*\[([^\]]+)\]:\s*(\S+)', re.MULTILINE)
HTML_A_HREF_RE = re.compile(r'<a\s+[^>]*href=["\']([^"\']+)["\']', re.IGNORECASE)
URL_SCHEME_RE = re.compile(r'^[a-zA-Z][a-zA-Z0-9+.-]*:')  # Match scheme:
IGNORED_SCHEMES = ('mailto:', 'tel:', 'javascript:', 'data:')

# Default parameters for HTTP checks
DEFAULT_TIMEOUT = 5  # seconds
DEFAULT_CONCURRENCY = 20
USER_AGENT = "check-links-script/1.0 (+https://github.com/)"

# ---------- Utility Functions ----------
def is_external_link(url: str) -> bool:
    return url.startswith('http://') or url.startswith('https://')

def is_ignored_scheme(url: str) -> bool:
    lower = url.lower()
    return any(lower.startswith(s) for s in IGNORED_SCHEMES)

def normalize_fs_path(path: str) -> str:
    # Remove query or fragment (for internal file links)
    p = path.split('?')[0].split('#')[0]
    return os.path.normpath(p)

def github_anchor_from_heading(text: str) -> str:
    """
    Generate anchor from heading text based on GitHub style (approximate implementation).
    - Convert to lower case
    - Remove punctuation (keep spaces and hyphens)
    - Replace spaces with -
    - Merge consecutive -
    This does not cover all edge-cases, but works for common headings.
    """
    t = text.strip().lower()
    # Remove punctuation usually removed by GitHub (keep alphanumeric, spaces, -, _)
    t = re.sub(r'[^\w\s\-]', '', t, flags=re.UNICODE)
    t = re.sub(r'\s+', '-', t)
    t = re.sub(r'-{2,}', '-', t)
    return t

def extract_markdown_headings(content: str) -> List[str]:
    headings = []
    for line in content.splitlines():
        m = re.match(r'^\s{0,3}(#{1,6})\s+(.*)$', line)
        if m:
            headings.append(m.group(2).strip())
    return headings

# ---------- Link Extraction ----------
def extract_links_from_markdown(content: str) -> List[str]:
    links = []
    links.extend(MARKDOWN_INLINE_LINK_RE.findall(content))
    links.extend(MARKDOWN_AUTOLINK_RE.findall(content))
    # reference definitions
    for m in MARKDOWN_REF_DEF_RE.findall(content):
        # m => (id, url)
        links.append(m[1])
    return links

def extract_links_from_html(content: str) -> List[str]:
    return HTML_A_HREF_RE.findall(content)

def extract_links_from_file(path: str) -> List[str]:
    ext = os.path.splitext(path)[1].lower()
    with open(path, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
    links = []
    if ext in ('.md', '.markdown', '.rst', '.txt'):
        links = extract_links_from_markdown(content)
    elif ext in ('.html', '.htm'):
        links = extract_links_from_html(content)
    else:
        # Heuristic: try extracting as markdown first, then as html
        links = extract_links_from_markdown(content) + extract_links_from_html(content)
    return links

# ---------- Check Functions ----------
class LinkResult:
    def __init__(self, source_file: str, link: str, kind: str):
        self.source_file = source_file
        self.link = link
        self.kind = kind  # 'internal' or 'external' or 'ignored'
        self.ok = True
        self.detail = ""  # Error message or status code, etc.

    def to_dict(self):
        return {
            "source": self.source_file,
            "link": self.link,
            "kind": self.kind,
            "ok": self.ok,
            "detail": self.detail
        }

def check_internal_link(source_file: str, link: str, repo_root: str, check_anchor: bool=True) -> LinkResult:
    """
    Verify if the file pointed to by the internal link exists; if it contains an anchor, optionally verify if the anchor exists (simple match for Markdown '#' headings only).
    """
    res = LinkResult(source_file, link, 'internal')
    # Anchor
    if link.startswith('#'):
        # Anchor to current file
        target_path = source_file
        anchor = link[1:]
    else:
        parts = link.split('#', 1)
        rel_path = parts[0] if parts[0] != '' else '.'
        anchor = parts[1] if len(parts) > 1 else None
        # Absolute path (starts with /) relative to repo_root, otherwise relative to source_file directory
        if rel_path.startswith('/'):
            target_path = os.path.join(repo_root, rel_path.lstrip('/'))
        else:
            source_dir = os.path.dirname(source_file)
            target_path = os.path.normpath(os.path.join(source_dir, rel_path))

    # If link points to a directory, try appending index.md or README.md
    if os.path.isdir(target_path):
        for candidate in ('index.md', 'README.md', 'index.html'):
            p = os.path.join(target_path, candidate)
            if os.path.exists(p):
                target_path = p
                break

    if not os.path.exists(target_path):
        res.ok = False
        res.detail = f'file not found: {target_path}'
        return res

    if anchor and check_anchor:
        # Try to find matching anchor in target file headings (Markdown headings only)
        try:
            with open(target_path, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
            headings = extract_markdown_headings(content)
            generated = {github_anchor_from_heading(h) for h in headings}
            # Compare anchors (anchor might be URL encoded or lower case)
            anchor_norm = anchor.strip().lower()
            anchor_norm = re.sub(r'%20', '-', anchor_norm)  # decode common encoding for spaces
            if anchor_norm not in generated:
                res.ok = False
                res.detail = f'anchor not found: #{anchor} in {target_path} (found {len(generated)} headings)'
        except Exception as e:
            # Do not block on read/parse failure, give warning
            res.ok = False
            res.detail = f'failed to read/parse for anchor check: {e}'
    else:
        res.ok = True
    return res

# Cache external link check results to avoid duplicate requests
_external_cache_lock = threading.Lock()
_external_cache: Dict[str, Tuple[bool, str]] = {}

def check_external_link_head(url: str, timeout: float) -> Tuple[bool, str]:
    """
    Use HTTP HEAD first. If HEAD returns 405/501 etc., fallback to GET (stream).
    Returns (ok, detail)
    """
    headers = {
        "User-Agent": USER_AGENT,
    }
    try:
        r = requests.head(url, allow_redirects=True, timeout=timeout, headers=headers)
        status = r.status_code
        if 200 <= status < 400:
            return True, f'HTTP {status}'
        if status in (405, 501):  # method not allowed / not implemented
            # fallback
            r = requests.get(url, allow_redirects=True, timeout=timeout, headers=headers, stream=True)
            status = r.status_code
            if 200 <= status < 400:
                r.close()
                return True, f'HTTP {status} (GET fallback)'
            else:
                r.close()
                return False, f'HTTP {status} (GET fallback)'
        else:
            return False, f'HTTP {status}'
    except requests.exceptions.SSLError as e:
        return False, f'SSL error: {e}'
    except requests.exceptions.Timeout:
        return False, 'timeout'
    except requests.exceptions.ConnectionError as e:
        return False, f'connection error: {e}'
    except Exception as e:
        return False, f'error: {e}'

def check_external_link_cached(url: str, timeout: float) -> Tuple[bool, str]:
    with _external_cache_lock:
        if url in _external_cache:
            return _external_cache[url]
    ok, detail = check_external_link_head(url, timeout)
    with _external_cache_lock:
        _external_cache[url] = (ok, detail)
    return ok, detail

def gather_doc_files(root: str, paths: List[str], exts: Set[str], file_patterns: List[str] = None, ignore_file_patterns: List[str] = None) -> List[str]:
    found = []
    if not paths:
        paths = [root]
    
    # helper for pattern matching
    def matches_any(path_str: str, patterns: List[str]) -> bool:
        if not patterns:
            return False
        name = os.path.basename(path_str)
        return any(fnmatch.fnmatch(path_str, p) or fnmatch.fnmatch(name, p) for p in patterns)

    for p in paths:
        full = os.path.join(root, p) if not os.path.isabs(p) and root else p
        
        candidates = []
        if os.path.isfile(full):
            candidates.append(full)
        elif os.path.isdir(full):
            for dirpath, dirnames, filenames in os.walk(full):
                for fn in filenames:
                    candidates.append(os.path.join(dirpath, fn))
        
        for f_path in candidates:
            # use relative path for matching if possible, or name
            rel_path = os.path.relpath(f_path, root)
            
            # 1. Check ignore patterns
            if ignore_file_patterns and matches_any(rel_path, ignore_file_patterns):
                continue
            
            # 2. Check allow patterns (if provided, strictly follow them; otherwise use extensions)
            if file_patterns:
                if matches_any(rel_path, file_patterns):
                    found.append(os.path.normpath(f_path))
            else:
                # default behavior: check extension
                if os.path.splitext(f_path)[1].lower().lstrip('.') in exts:
                    found.append(os.path.normpath(f_path))

    return sorted(set(found))

# ---------- Main Process ----------
def main(argv=None):
    parser = argparse.ArgumentParser(description="Check documentation links (internal paths & external availability)")
    parser.add_argument('--root', '-r', default='.', help="Repository/project root directory (for resolving internal links starting with '/')")
    parser.add_argument('--paths', '-p', nargs='*', default=['docs', '.'],
                        help="Files or directories to scan (relative to root), can be specified multiple times; default ['docs', '.'], only scans specified extensions")
    parser.add_argument('--ext', default='md,markdown,html,rst',
                        help="File extensions to scan, comma separated (without dot), default md,markdown,html,rst")
    parser.add_argument('--concurrency', '-c', type=int, default=DEFAULT_CONCURRENCY,
                        help=f"Concurrent external link checks (default {DEFAULT_CONCURRENCY})")
    parser.add_argument('--timeout', '-t', type=float, default=DEFAULT_TIMEOUT,
                        help=f"HTTP request timeout (seconds), default {DEFAULT_TIMEOUT}")
    parser.add_argument('--no-anchor-check', dest='anchor_check', action='store_false',
                        help="Disable internal link anchor check (faster)")
    parser.add_argument('--files', nargs='*', help="Only check files matching these glob patterns (e.g. *.md, docs/*)")
    parser.add_argument('--ignore-files', nargs='*', help="Ignore files matching these glob patterns")
    parser.add_argument('--links', nargs='*', help="Only check links starting with these prefixes (e.g. http, https)")
    parser.add_argument('--ignore-links', nargs='*', help="Ignore links starting with these prefixes")
    parser.add_argument('--verbose', '-v', action='store_true')
    args = parser.parse_args(argv)

    logging.basicConfig(level=logging.DEBUG if args.verbose else logging.INFO,
                        format='[%(levelname)s] %(message)s')

    root = os.path.abspath(args.root)
    exts = {e.strip().lower() for e in args.ext.split(',') if e.strip()}
    
    files = gather_doc_files(
        root, 
        args.paths, 
        exts, 
        file_patterns=args.files, 
        ignore_file_patterns=args.ignore_files
    )
    if not files:
        logging.error("No document files found (check root/paths/ext settings)")
        return 2

    logging.info(f"Scanning {len(files)} files (and extracting links)...")
    # Link set: source -> [links]
    per_file_links: Dict[str, List[str]] = {}
    for f in files:
        try:
            links = extract_links_from_file(f)
            per_file_links[f] = links
        except Exception as e:
            logging.warning(f"Failed to read or parse file: {f} : {e}")
            per_file_links[f] = []

    # Categorize links, prepare for check
    internal_tasks = []
    external_urls: Set[str] = set()
    ignored = []

    for src, links in per_file_links.items():
        for l in links:
            l = l.strip()
            if not l:
                continue

            # User ignore rules
            if args.ignore_links and any(l.startswith(p) for p in args.ignore_links):
                ignored.append((src, l))
                continue
            
            # User allow rules (if provided)
            if args.links and not any(l.startswith(p) for p in args.links):
                ignored.append((src, l))
                continue

            # Exclude schemes like mailto/tel
            if is_ignored_scheme(l):
                ignored.append((src, l))
                continue
            if is_external_link(l):
                external_urls.add(l)
            else:
                internal_tasks.append((src, l))

    logging.info(f"Found links: internal={len(internal_tasks)} external_unique={len(external_urls)} ignored={len(ignored)}")

    # 1) Check internal links (synchronous is fine)
    internal_results: List[LinkResult] = []
    for src, l in internal_tasks:
        r = check_internal_link(src, l, root, check_anchor=args.anchor_check)
        internal_results.append(r)

    # 2) Concurrent check for external links
    external_results: List[LinkResult] = []
    if external_urls:
        logging.info(f"Concurrent check for external links (concurrency={args.concurrency} timeout={args.timeout}s)...")
        with ThreadPoolExecutor(max_workers=args.concurrency) as ex:
            future_map = {ex.submit(check_external_link_cached, url, args.timeout): url for url in external_urls}
            for fut in as_completed(future_map):
                url = future_map[fut]
                ok, detail = fut.result()
                # To output source, we need to map back to all locations where url is referenced in source files
                for src, links in per_file_links.items():
                    if url in links:
                        lr = LinkResult(src, url, 'external')
                        lr.ok = ok
                        lr.detail = detail
                        external_results.append(lr)

    # collect ignored as LinkResult
    ignored_results = [LinkResult(src, l, 'ignored') for src, l in ignored]
    for ig in ignored_results:
        ig.ok = True
        ig.detail = 'ignored scheme'

    # Merge and summarize
    all_results = internal_results + external_results + ignored_results
    broken = [r for r in all_results if not r.ok]

    # Output results
    if broken:
        logging.error(f"Detected {len(broken)} broken links:")
        for r in broken:
            print(f"- {r.source_file} -> {r.link}  [{r.kind}]  : {r.detail}")
    else:
        logging.info("All checks passed: No broken links found.")

    # Brief summary
    total_links = sum(len(v) for v in per_file_links.values())
    print()
    print("Summary:")
    print(f"  files scanned: {len(files)}")
    print(f"  total links found: {total_links}")
    print(f"  internal checked: {len(internal_results)}")
    print(f"  external checked (unique): {len(external_urls)}")
    print(f"  ignored: {len(ignored)}")
    print(f"  broken: {len(broken)}")

    # Optional: Write results to file (JSON/CSV) - left for user extension

    return 1 if broken else 0

if __name__ == '__main__':
    sys.exit(main())
