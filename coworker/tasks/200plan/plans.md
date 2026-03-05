# Plans

- PerceptiveAgent#act 支持 browser-cli 命令，直接解析，不走 LLM，省钱。
- 优化 browser-cli 返回结果，同 PerceptiveAgent#act 执行后的结果一致。
- 优化 coworker 下所有脚本下设置工作目录的方式，抛弃使用 git rev-parse 获取路径，改为使用相对路径 + 配置文件。
  - 配置文件设置工作目录
  - 脚本定位使用相对路径
