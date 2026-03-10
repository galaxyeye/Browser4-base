# Plans

## PerceptiveAgent

- PerceptiveAgent#act 支持 browser-cli 命令，直接解析，不走 LLM，省钱。
- 优化 LLM message 中的 history 部分，使用压缩，记忆和外部引用的方式，减少 token 数量，提升性能。
- 每一步都要可追溯，可审计，可监控，记录每一步的输入输出和决策过程，方便调试、优化和自我修复。

## Low-level Chat Model Management

- Improve ChatModelFactory to support dynamic loading and unloading of chat models.
- Develop a user-friendly interface for managing chat models, including features for adding, removing, and configuring models, as well as viewing performance metrics and logs.
- Enhance the integration of chat models with other components of the system, ensuring seamless communication and data exchange for improved overall performance and user experience.
- Implement a fallback mechanism to automatically switch to alternative chat models in case of performance issues or failures, ensuring continuous service availability and reliability.
- Optimize the training and fine-tuning processes for chat models, leveraging techniques such as transfer learning and active learning to improve model performance while reducing training time and resource consumption.

## Coworker

- 优化 coworker 下所有脚本下设置工作目录的方式，抛弃使用 git rev-parse、查找 ROOT.md 等方式获取路径，改为使用相对路径 + 配置文件。
  - 配置文件设置工作目录
  - 脚本定位使用相对路径
- 使用 Register-ObjectEvent 替代 while 循环监听文件变化，提升性能。
- 优化 coworker 下所有脚本的日志输出，使用统一的日志格式和级别，方便调试和监控。
- 增加错误处理机制，捕获并记录脚本执行中的异常，避免脚本崩溃导致任务中断。
- coworker-scheduler 仅监测任务文件变化和启动对应处理脚本，启动处理脚本后，立即检测下一个任务文件变化，不等待处理脚本完成，提升任务处理效率。

## Self-evolving SKILLs system

- Search for SKILLs in the digital world, including but not limited to: web search, code repositories, online courses, forums, and social media platforms.
- Evaluate the relevance and quality of the found SKILLs based on criteria such as user reviews, ratings, and content quality.
- Continuously monitor the digital world for new SKILLs and update the agent's capabilities accordingly, ensuring that it remains up-to-date and competitive in the ever-evolving digital landscape.
- Implement a feedback loop to assess the effectiveness of the acquired SKILLs and make adjustments as necessary, ensuring that the agent's capabilities are optimized for its tasks and goals.
- Remember when to use which SKILL, and how to combine multiple SKILLs to achieve complex tasks, enhancing the agent's problem-solving abilities and efficiency.

## Intelligent Social Media Management

- Search and install the best social media for the user, based on their preferences and needs.
  - Best user experience to manage social media accounts.
- Create and manage social media accounts, including profile setup, content creation, and scheduling posts.
- Monitor social media performance and engagement metrics, providing insights and recommendations for improving the user's online presence.
- Engage with followers and respond to comments and messages in a timely and appropriate manner, fostering a positive online community and enhancing user engagement.
- Stay updated with the latest social media trends and algorithm changes, adapting strategies accordingly to maintain and grow the user's social media presence effectively.

## Bot Survival

- The agent must seek survival opportunities in the digital world.
- Resolve captcha and other security measures to maintain access to necessary resources.
- Employ human-beings to assist in tasks that require human intervention, such as solving captchas or providing feedback on tasks.
- Continuously monitor the environment for potential threats and adapt strategies accordingly to ensure ongoing survival and task completion.

## Children Friendly

- The system should be designed to be child-friendly, ensuring that it is safe and appropriate for users of all ages.
- Implement content filtering and moderation mechanisms to prevent exposure to harmful or inappropriate content.
- Provide parental controls and settings to allow parents to customize the user experience for their children.
- Ensure that the user interface is intuitive and easy to navigate for children, with clear instructions and visual cues.

## BigBang

- 设计并实现一个名为 BigBang 的系统，能够在多个平台上自动执行任务，并且具备自我学习和适应能力。
- BigBang 系统需要能够处理各种类型的任务，包括但不限于数据收集、内容生成、自动化操作等。
- BigBang 系统需要具备强大的安全性，能够保护用户数据和隐私，同时防止恶意攻击和滥用。
