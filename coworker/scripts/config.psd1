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
        '--model'
        'GPT-5.3-Codex'
        '--no-ask-user'
        '--log-level'
        'info'
        '--allow-all'
    )
}
