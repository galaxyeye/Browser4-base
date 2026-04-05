#!/usr/bin/env python3
import os
import shutil
import argparse
import time
import sys

def get_timestamp():
    return time.strftime("%Y%m%d_%H%M%S")

def normalize_path_slashes(path):
    return path.replace('\\', '/')

def find_references_and_replace(root_dir, replacements, dry_run=False):
    """
    replacements: list of tuple (old_str, new_str)
    """
    exclude_dirs = {'.git', 'target', 'node_modules', '.idea', 'build', 'dist', '__pycache__'}
    count = 0
    
    # Pre-calculate replacements to avoid repeated checks if list is long
    # But here we just iterate
    
    print(f"Scanning for references in {root_dir}...")
    
    for dirpath, dirnames, filenames in os.walk(root_dir):
        # Exclude dirs
        dirnames[:] = [d for d in dirnames if d not in exclude_dirs]
        
        for filename in filenames:
            # Skip binary files or unlikely candidates
            if filename.endswith(('.jar', '.class', '.exe', '.dll', '.png', '.jpg', '.zip', '.gz', '.ico', '.pdf', '.pyc')):
                continue
            
            # Skip the script itself if we are running from within the repo
            if filename == os.path.basename(__file__):
                continue

            filepath = os.path.join(dirpath, filename)
            try:
                with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                
                new_content = content
                modified = False
                
                # Apply replacements
                # We do this sequentially. Order might matter if one string is substring of another.
                # Ideally sort by length descending to avoid partial matches?
                # But here we are replacing full relative paths usually.
                
                for old_str, new_str in replacements:
                    if old_str in new_content:
                        # Check word boundaries?
                        # No, paths are often part of longer strings.
                        # But we want to avoid replacing 'bin/foo.sh' inside 'bin/foo.sh.bak' if we are not careful?
                        # Actually simple string replacement is what was requested.
                        
                        new_content = new_content.replace(old_str, new_str)
                        modified = True
                        print(f"  [Match] Found '{old_str}' in {filepath}")
                
                if modified:
                    if not dry_run:
                        with open(filepath, 'w', encoding='utf-8') as f:
                            f.write(new_content)
                        print(f"  [Update] Updated references in: {filepath}")
                    else:
                        print(f"  [DryRun] Would update references in: {filepath}")
                    count += 1
            except Exception as e:
                print(f"Error processing {filepath}: {e}")
    return count

def main():
    parser = argparse.ArgumentParser(description="Move scripts and update references safely.")
    parser.add_argument('source', help="Source directory containing scripts")
    parser.add_argument('dest', help="Destination directory")
    parser.add_argument('--root', default='.', help="Project root for updating references (default: current dir)")
    parser.add_argument('--files', nargs='+', help="Specific files to move (default: move all .ps1, .sh, .py)")
    parser.add_argument('--dry-run', action='store_true', help="Do not move or update files, just show what would happen")
    
    args = parser.parse_args()
    
    source_dir = os.path.abspath(args.source)
    dest_dir = os.path.abspath(args.dest)
    root_dir = os.path.abspath(args.root)
    
    if not os.path.exists(source_dir):
        print(f"Error: Source directory {source_dir} does not exist.")
        sys.exit(1)
    
    if not os.path.exists(dest_dir):
        if not args.dry_run:
            try:
                os.makedirs(dest_dir)
                print(f"Created destination directory: {dest_dir}")
            except OSError as e:
                print(f"Error creating destination directory: {e}")
                sys.exit(1)
        else:
            print(f"[DryRun] Would create directory {dest_dir}")

    # Find scripts
    scripts = []
    
    if args.files:
        # Use provided list
        for f in args.files:
            if os.path.isfile(os.path.join(source_dir, f)):
                scripts.append(f)
            else:
                print(f"Warning: File {f} not found in {source_dir}, skipping.")
    else:
        # Scan directory
        try:
            for f in os.listdir(source_dir):
                if f.endswith(('.ps1', '.sh', '.py')) and os.path.isfile(os.path.join(source_dir, f)):
                    scripts.append(f)
        except OSError as e:
            print(f"Error reading source directory: {e}")
            sys.exit(1)
            
    if not scripts:
        print(f"No scripts found to move in {source_dir}.")
        # We exit successfully even if no scripts found, as task is done (nothing to move)
        return

    print(f"Found {len(scripts)} scripts to move.")

    replacements = [] # Store all replacements to perform

    for script in scripts:
        src_path = os.path.join(source_dir, script)
        
        # Determine new filename
        new_filename = script
        dest_path = os.path.join(dest_dir, new_filename)
        
        # Check collision
        if os.path.exists(dest_path):
            base, ext = os.path.splitext(script)
            timestamp = get_timestamp()
            new_filename = f"{base}_{timestamp}{ext}"
            dest_path = os.path.join(dest_dir, new_filename)
            print(f"Collision detected for {script}. Renaming to {new_filename}")
            
        # Calculate relative paths for replacement
        rel_source = os.path.relpath(src_path, root_dir)
        rel_dest = os.path.relpath(dest_path, root_dir)
        
        # Generate replacements for both Slash and Backslash styles
        rel_source_fwd = normalize_path_slashes(rel_source)
        rel_dest_fwd = normalize_path_slashes(rel_dest)
        
        rel_source_back = rel_source.replace('/', '\\')
        rel_dest_back = rel_dest.replace('/', '\\')
        
        print(f"Planning move: {script} -> {new_filename}")
        
        replacements.append((rel_source_fwd, rel_dest_fwd))
        if rel_source_fwd != rel_source_back:
            replacements.append((rel_source_back, rel_dest_back))
            
        if not args.dry_run:
            try:
                shutil.move(src_path, dest_path)
                print(f"Moved: {src_path} -> {dest_path}")
            except Exception as e:
                print(f"Error moving {script}: {e}")
        else:
            print(f"[DryRun] Would move {src_path} -> {dest_path}")

    # Apply all replacements
    if replacements:
        find_references_and_replace(root_dir, replacements, dry_run=args.dry_run)
    else:
        print("No replacements to make.")

if __name__ == "__main__":
    main()
