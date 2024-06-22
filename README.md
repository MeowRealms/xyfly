This is my first plugin, and its functionality is very simple. It allows players to have temporary flight ability. Currently, the time is measured in seconds. More time formats will be added in the future. The time only decreases when the player is flying. This plugin has only been tested in Minecraft version 1.21.
The plugin is still under development, and more features will be implemented soon.

这是我的第一个插件，他的功能非常简单，仅仅是可以给予玩家临时的飞行能力，目前时间采用秒计时，后续会增加更多计时样式。只有玩家起飞才会减少时间，本插件仅在我的世界1.21版本中进行了测试。
插件依旧在开发当中，更多功能即将实现。

Plugin Introduction
Plugin Name: Xyfly
Plugin Description:
Xyfly is a Minecraft server plugin designed to manage flight time for players. It allows server administrators to set flight time limits for players and provides real-time updates on the remaining flight time while players are flying. The plugin supports multiple languages, allowing administrators to choose different language files through the configuration file to better suit servers in different languages.
Implemented Features
Flight Time Management:
Administrators can use the command /xyfly settime <player> <time(seconds)> to set flight time for a specified player.
Players can use the command /xyfly on to enable flight mode, and the flight time will count down according to the settings in the configuration file.
Players can use the command /xyfly off to disable flight mode, pausing the flight time.
Flight Time Query:
Administrators and players can use the command /xyfly gettime <player> to query the remaining flight time of a specified player.
Configuration File Management:
The plugin generates a default config.yml configuration file upon first startup.
Administrators can select the language and customize the flight time messages through the configuration file.
Using the command /xyfly reload reloads the configuration and language files, ensuring that changes to the configuration take effect immediately.
Multi-language Support:
The plugin supports multiple languages, allowing administrators to select different language files through the language option in config.yml.
The plugin provides default English (en.yml) and Chinese (zh.yml) language files, and administrators can add other language files as needed.
Event Listener:
The plugin listens for changes in players' flight status, providing real-time updates and displaying the remaining flight time.
The plugin listens for player quit events to ensure that flight tasks are stopped and flight time data is saved when players leave the server.
Future Feature Plans
More Language Support:
Add more language files to support a wider range of languages to meet the needs of different server administrators.
Flight Time Reward System:
Add a flight time reward system where players can earn additional flight time by completing certain tasks or achieving certain milestones.
GUI Interface:
Provide a graphical user interface (GUI) to allow administrators and players to manage and view flight time more intuitively.
Flight Time Purchase System:
Add a flight time purchase system where players can buy additional flight time using in-game currency or other resources.
Flight Time Logging:
Add logging for flight time changes, enabling administrators to view and manage flight time changes for players.
Advanced Permission Management:
Add more granular permission management, allowing administrators to set different flight time permissions for different groups of players.
API Interface:
Provide an API interface for the plugin to facilitate integration and extension with other plugins.
With these features, the Xyfly plugin will offer more flexible and rich flight time management functions for Minecraft servers, enhancing the gaming experience for players. If you have any questions or suggestions, feel free to reach out!



插件介绍
插件名称：Xyfly
插件描述：
Xyfly 是一个为 Minecraft 服务器提供飞行时间管理功能的插件。该插件允许服务器管理员为玩家设置飞行时间限制，并在玩家飞行时实时更新剩余飞行时间。插件支持多语言功能，管理员可以通过配置文件选择不同的语言文件，以便更好地适应不同语言的服务器环境。
已实现的功能
飞行时间管理：
管理员可以使用 /xyfly settime <玩家> <时间(秒)> 命令为指定玩家设置飞行时间。
玩家可以使用 /xyfly on 命令开启飞行模式，飞行时间会根据配置文件中的设置进行倒计时。
玩家可以使用 /xyfly off 命令关闭飞行模式，飞行时间暂停。
飞行时间查询：
管理员和玩家可以使用 /xyfly gettime <玩家> 命令查询指定玩家的剩余飞行时间。
配置文件管理：
插件会在首次启动时生成默认的 config.yml 配置文件。
管理员可以通过配置文件选择语言，并自定义飞行时间的提示消息。
使用 /xyfly reload 命令重新加载配置文件和语言文件，确保配置的更改即时生效。
多语言支持：
插件支持多语言功能，管理员可以通过 config.yml 文件中的 language 选项选择不同的语言文件。
插件默认提供了英文（en.yml）和中文（zh.yml）语言文件，管理员可以根据需要自行添加其他语言文件。
事件监听：
插件监听玩家飞行状态的变化，实时更新并显示剩余飞行时间。
插件监听玩家退出事件，确保玩家退出时停止飞行任务，保存飞行时间数据。
未来功能规划
更多语言支持：
增加更多语言文件，支持更多的语言，以满足不同服务器管理员的需求。
飞行时间奖励系统：
增加飞行时间奖励系统，例如玩家完成一定任务或达到一定成就后可以获得额外的飞行时间。
GUI 界面：
提供一个图形用户界面（GUI），方便管理员和玩家更直观地管理和查看飞行时间。
飞行时间购买系统：
增加飞行时间购买系统，玩家可以通过游戏内货币或其他资源购买额外的飞行时间。
飞行时间日志记录：
增加飞行时间的日志记录功能，记录玩家的飞行时间变化情况，方便管理员查看和管理。
高级权限管理：
增加更细化的权限管理，允许管理员设置不同组别的玩家拥有不同的飞行时间权限。
API 接口：
提供插件的 API 接口，方便其他插件与 Xyfly 进行集成和扩展。
通过这些功能的实现，Xyfly 插件将为 Minecraft 服务器提供更加灵活和丰富的飞行时间管理功能，提升玩家的游戏体验。如果有任何问题或建议，欢迎随时联系我！
