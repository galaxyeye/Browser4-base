# Finish script delete-copilot-branches

Delete branches created by GitHub Copilot, which are named "copilot/*".

1. remove local branches that start with "copilot/"
2. remove remote branches that start with " remotes/origin/copilot/", need human confirmation before deleting each remote branch

[delete-copilot-branches.ps1](../../../bin/git/delete-copilot-branches.ps1)
