# Improve TimeZone Handling in coworker scripts

When generate date-time based file paths or names, ensure that the scripts correctly handle time zones.
This is crucial for generating memory files with accurate timestamps, especially if the coworker is used across different regions.

## Solution Approach

1. **Use UTC Time**: Standardize on using UTC time for all date-time operations in the scripts. This avoids issues with local time zones and daylight saving time changes.
2. **Explicit Time Zone Handling**: If local time is necessary, ensure that the scripts explicitly handle time zones using appropriate libraries or commands (e.g., `Get-Date -Format "yyyyMMdd" -AsUTC` in PowerShell or `date -u +"%Y%m%d"` in Bash).
3. **Testing**: Test the scripts in different time zones to verify that the generated file paths and names are correct and consistent.

