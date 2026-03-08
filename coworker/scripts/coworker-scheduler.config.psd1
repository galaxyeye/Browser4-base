@{
    Scheduler = @{
        TickSeconds          = 5
        PowerShellExecutable = 'pwsh'
        LogDirectory         = 'coworker\tasks\300logs\scheduler'
        StatusFile           = 'coworker\tasks\300logs\scheduler\scheduled-tasks.status.json'
    }

    Tasks = @(
        @{
            Name            = 'coworker'
            Description     = 'Process queued coworker tasks.'
            Enabled         = $true
            IntervalSeconds = 15
            DependsOn       = @('task-source-monitor')
            ScriptPath      = 'coworker\scripts\deprecated\run_coworker_periodically.ps1'
            Arguments       = @('-Once')
        }
        @{
            Name            = 'draft-refinement'
            Description     = 'Process the draft refinement queue.'
            Enabled         = $true
            IntervalSeconds = 15
            ScriptPath      = 'coworker\scripts\deprecated\run_draft_refinement_periodically.ps1'
            Arguments       = @('-Once')
        }
        @{
            Name            = 'task-source-monitor'
            Description     = 'Poll configured task sources and dispatch new tasks.'
            Enabled         = $false
            IntervalSeconds = 60
            ScriptPath      = 'coworker\scripts\deprecated\task-source-monitor.ps1'
            Arguments       = @('-Once')
        }
    )
}

