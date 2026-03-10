## Improve Coworker

- 优化 coworker 下所有脚本下设置工作目录的方式，抛弃使用 git rev-parse、查找 ROOT.md 等方式获取路径，改为使用相对路径 + 配置文件。
    - 配置文件设置工作目录
    - 脚本定位使用相对路径

Coworker-scheduler 负责读取工作目录，监测任务文件变化，启动对应处理脚本，启动处理脚本后，立即检测下一个任务文件变化，不等待处理脚本完成，提升任务处理效率。
Coworker 下的所有其他脚本不再使用 Set-Location 设置工作目录，而是保持调用者的当前目录不变，使用相对路径访问文件和资源。

[coworker-scheduler.ps1](../../scripts/coworker-scheduler.ps1)
