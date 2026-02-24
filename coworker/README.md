# Builtin AI Coworker

## Coworker

Put you tasks in `copilot/tasks/1created` and run `coworker.ps1`/`coworker.sh` to do the job.

Best practice:

1. Write your draft in 0prepare
2. Once you are satisfied with the draft, move it to 1created and run coworker to execute the task
3. Once the task is done, you can find the result in 3complete, and then you can execute `commit.ps1` to commit and push the changes to your repository.

You can also run coworker periodically:

Windows:

```shell
coworker/scripts/run_coworker_periodically.ps1
```

Linux:
```shell
coworker/scripts/run_coworker_periodically.sh
```

## Commit and Push

After coworker finishes the tasks, you can commit and push the changes to your repository:

```shell
coworker/scripts/commit.ps1
```
