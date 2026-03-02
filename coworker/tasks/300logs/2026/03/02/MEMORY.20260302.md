# Daily Memory - 2026-03-02

## Tasks

### Finish script delete-copilot-branches
- **Goal**: Complete the script `bin/git/delete-copilot-branches.ps1` to delete branches starting with `copilot/`.
- **Outcome**: Successfully implemented the script to list and delete matching branches using `git branch -D`. Tested with a dummy branch.
- **Lessons**: When parsing `git branch` output, whitespace and current branch indicator (*) need to be handled carefully.
