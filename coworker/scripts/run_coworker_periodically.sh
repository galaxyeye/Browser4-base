#!/usr/bin/env bash

# Find repo root
REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null)
if [ -z "$REPO_ROOT" ]; then
    echo "Repo root not found. Exiting."
    exit 1
fi

cd "$REPO_ROOT" || exit 1

SCRIPT_PATH="$REPO_ROOT/coworker/scripts/coworker.sh"
SCRIPT_NAME="coworker.sh"
WRAPPER_NAME="run_coworker_periodically.sh"
MONITOR_SCRIPT_PATH="$REPO_ROOT/coworker/scripts/task-source-monitor.sh"
MONITOR=false

# Parse arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    --monitor)
      MONITOR=true
      shift
      ;;
    *)
      # Ignore unknown arguments for now or treat them as errors
      echo "Unknown argument: $1"
      shift
      ;;
  esac
done

echo "Monitoring $SCRIPT_NAME..."
echo "Script path: $SCRIPT_PATH"
if [ "$MONITOR" = true ]; then
  echo "Task source monitoring enabled using: $MONITOR_SCRIPT_PATH"
fi


while true; do
    # Check for tasks in 1created or 5approved
    HAS_TASKS=false

    # Check 1created
    if [ -d "coworker/tasks/1created" ]; then
        if [ "$(ls -A coworker/tasks/1created)" ]; then
            HAS_TASKS=true
        fi
    fi

    # Check 5approved if no tasks found yet
    if [ "$HAS_TASKS" = false ] && [ -d "coworker/tasks/5approved" ]; then
        # Check recursively for any files in 5approved
        if find coworker/tasks/5approved -type f | grep -q .; then
            HAS_TASKS=true
        fi
    fi

    TIMESTAMP=$(date -u "+%Y-%m-%d %H:%M:%S")

    if [ "$MONITOR" = true ]; then
        echo "$TIMESTAMP - Running task source monitor..."
        if [ -x "$MONITOR_SCRIPT_PATH" ]; then
            "$MONITOR_SCRIPT_PATH" --once
        else
            bash "$MONITOR_SCRIPT_PATH" --once
        fi
    fi

    if [ "$HAS_TASKS" = false ]; then
        echo "$TIMESTAMP - No tasks found in 1created or 5approved. Skipping check."
        sleep 15
        continue
    fi

    # Check if script is running
    # We look for processes matching the script name, but exclude this wrapper script
    IS_RUNNING=false

    # Get PIDs of processes matching the script name
    PIDS=$(pgrep -f "$SCRIPT_NAME")

    if [ -n "$PIDS" ]; then
        for pid in $PIDS; do
            # Skip if it's the current process
            if [ "$pid" = "$$" ]; then
                continue
            fi

            # Get the command line for this PID
            CMDLINE=$(ps -p "$pid" -o args= 2>/dev/null)

            # Check if it contains SCRIPT_NAME and does NOT contain WRAPPER_NAME
            if [[ "$CMDLINE" == *"$SCRIPT_NAME"* ]] && [[ "$CMDLINE" != *"$WRAPPER_NAME"* ]]; then
                IS_RUNNING=true
                break
            fi
        done
    fi

    if [ "$IS_RUNNING" = true ]; then
        echo "$TIMESTAMP - $SCRIPT_NAME is already running."
    else
        echo "$TIMESTAMP - $SCRIPT_NAME is NOT running. Starting it..."

        if [ -f "$SCRIPT_PATH" ]; then
            # Make sure it's executable
            if [ ! -x "$SCRIPT_PATH" ]; then
                chmod +x "$SCRIPT_PATH"
            fi

            # Run the script in background and monitor it
            "$SCRIPT_PATH" &
            COWORKER_PID=$!
            echo "Started $SCRIPT_NAME with PID: $COWORKER_PID"

            CONSECUTIVE_LOW_ACTIVITY=0
            MAX_CONSECUTIVE_LOW_ACTIVITY=18 # 3 minutes / 10 seconds

            while kill -0 $COWORKER_PID 2>/dev/null; do
                sleep 10

                # Find the latest copilot log file
                CURRENT_YEAR=$(date -u "+%Y")
                CURRENT_MONTH=$(date -u "+%m")
                CURRENT_DAY=$(date -u "+%d")
                LOGS_SUB_DIR="coworker/tasks/300logs/$CURRENT_YEAR/$CURRENT_MONTH/$CURRENT_DAY"

                if [ -d "$LOGS_SUB_DIR" ]; then
                    LATEST_LOG=$(ls -t "$LOGS_SUB_DIR"/*.copilot.log.stdout 2>/dev/null | head -n 1)

                    if [ -n "$LATEST_LOG" ]; then
                        # Check activity
                        # Use tail to get last 500 lines
                        # Count total lines and lines with "● "

                        LOG_CONTENT=$(tail -n 500 "$LATEST_LOG")
                        TOTAL_LINES=$(echo "$LOG_CONTENT" | wc -l)
                        ACTION_LINES=$(echo "$LOG_CONTENT" | grep -c "● ")

                        RATIO=0
                        if [ "$TOTAL_LINES" -gt 0 ]; then
                            # Use awk for floating point division
                            RATIO=$(awk "BEGIN {printf \"%.4f\", $ACTION_LINES/$TOTAL_LINES}")
                        fi

                        # Only check if enough lines > 10
                        if [ "$TOTAL_LINES" -gt 10 ]; then
                            # Check if ratio < 0.05
                            IS_LOW=$(awk "BEGIN {if ($RATIO < 0.05) print 1; else print 0}")

                            if [ "$IS_LOW" -eq 1 ]; then
                                CONSECUTIVE_LOW_ACTIVITY=$((CONSECUTIVE_LOW_ACTIVITY + 1))
                                echo "Warning: Low activity detected. Ratio: $RATIO ($ACTION_LINES/$TOTAL_LINES). Consecutive checks: $CONSECUTIVE_LOW_ACTIVITY/$MAX_CONSECUTIVE_LOW_ACTIVITY"
                            else
                                CONSECUTIVE_LOW_ACTIVITY=0
                            fi
                        fi

                        if [ "$CONSECUTIVE_LOW_ACTIVITY" -ge "$MAX_CONSECUTIVE_LOW_ACTIVITY" ]; then
                            echo "Error: Coworker loop detected! Killing process $COWORKER_PID..."
                            kill -9 $COWORKER_PID

                            # Abort task
                            LOG_NAME=$(basename "$LATEST_LOG")
                            # Extract task base name: HHmmss-TaskName.copilot.log.stdout
                            if [[ "$LOG_NAME" =~ ^[0-9]{6}-(.*)\.copilot\.log\.stdout$ ]]; then
                                TASK_BASE_NAME="${BASH_REMATCH[1]}"
                                echo "Aborting task: $TASK_BASE_NAME"

                                WORKING_DIR="coworker/tasks/2working"
                                ABORTED_DIR="coworker/tasks/3_5aborted"
                                mkdir -p "$ABORTED_DIR"

                                # Move matching files
                                find "$WORKING_DIR" -name "$TASK_BASE_NAME*" -exec mv {} "$ABORTED_DIR/" \; -exec echo "Moved task file to: {}" \;
                            else
                                echo "Could not determine task name from log file: $LOG_NAME"
                            fi
                            break
                        fi
                    fi
                fi
            done

            echo "Finished $SCRIPT_NAME execution."
        else
            echo "Error: Script not found at $SCRIPT_PATH"
        fi
    fi

    sleep 15
done
