@{
    Paths = @{
        WorkspaceRoot = '..\..'
        CoworkerRoot  = '..'
        TasksRoot     = '..\tasks'
    }

    Scheduler = @{
        WorkingDirectory = '..\..'
    }

    COPILOT = @(
        'gh'
        'copilot'
        '--no-ask-user'
        '--log-level'
        'info'
        '--allow-all'
    )
}
