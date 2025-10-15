# 核心方块模组 (Core Block Mod)

一个用于Minecraft 1.20.1 Forge的强大存储和合成模组。

## 功能特性

### 🎯 核心功能

1. **核心方块（Core Block）**
   - 2x2大小的多方块结构
   - 近乎无限的存储容量
   - 世界级共享存储系统
   - 每个核心实例提供8000个物品存储槽位

2. **存储系统**
   - 所有相同ID的核心共享同一个存储空间
   - 存储数据保存在世界数据中，而非方块NBT
   - 单种物品可存储8000个，容量随核心数量线性增长
   - 支持私有和公共核心模式

3. **合成功能**
   - 内置3x3工作台合成界面
   - 支持所有原版合成配方
   - 可以在核心GUI中直接合成物品

4. **物品浏览**
   - 滑动浏览界面显示所有存储的物品
   - 支持鼠标滚轮滚动
   - 实时显示物品数量

5. **能量生成**
   - 恒定100 FE/tick的能量输出
   - 兼容Forge能量系统
   - 可连接到其他需要能量的机器

6. **配置系统**
   - 可自定义核心ID
   - 公共/私有核心切换
   - 访问权限控制
   - 溢出物品处理选项（烧毁或保留）

7. **模组兼容**
   - 应用能源2 (Applied Energistics 2) 集成支持（框架已准备）
   - 精致存储 (Refined Storage) 集成支持（框架已准备）
   - 可作为AE2/RS网络的存储终端

## 构建说明

### 前置要求

- Java 17或更高版本
- Gradle 7.x或更高版本

### 构建步骤

1. 克隆仓库：
```bash
git clone <repository-url>
cd cursor-test
```

2. 构建模组：
```bash
./gradlew build
```

3. 生成的JAR文件位于：`build/libs/coremod-1.0.0.jar`

### 开发环境设置

1. 生成IDE项目文件：

对于IntelliJ IDEA:
```bash
./gradlew genIntellijRuns
```

对于Eclipse:
```bash
./gradlew eclipse
```

2. 在IDE中导入项目

## 使用方法

### 放置核心

1. 合成核心方块（配方：钻石+末影珍珠+箱子）
2. 在世界中放置核心方块
3. 右键点击打开GUI

### GUI界面

#### 合成模式
- 左侧：3x3合成网格
- 右侧：合成结果槽
- 底部：玩家背包

#### 浏览模式
- 显示核心中所有存储的物品
- 使用鼠标滚轮滚动查看更多物品
- 点击物品提取到背包

#### 设置页面
- 核心ID：设置核心的唯一标识符
- 公共核心：是否为公共核心
- 允许其他玩家：其他玩家是否可以访问
- 烧毁溢出物品：当移除核心导致容量减少时，是否销毁超出的物品

### 容量计算

每个核心实例提供8000个物品槽位：
- 1个核心 = 8000个物品
- 2个核心 = 16000个物品
- 3个核心 = 24000个物品
- 以此类推...

### 共享存储

具有相同核心ID的所有核心共享同一个存储空间：
- 公共核心：所有玩家的相同ID核心共享存储
- 私有核心：只有同一玩家的相同ID核心共享存储

## 文件结构

```
coremod/
├── src/main/java/com/coremod/
│   ├── CoreMod.java                 # 主模组类
│   ├── block/
│   │   └── CoreBlock.java           # 核心方块
│   ├── blockentity/
│   │   └── CoreBlockEntity.java     # 核心方块实体
│   ├── client/
│   │   ├── ClientSetup.java         # 客户端设置
│   │   └── screen/
│   │       ├── CoreScreen.java      # 主GUI界面
│   │       └── CoreConfigScreen.java # 配置GUI
│   ├── compat/
│   │   ├── ae2/
│   │   │   └── AE2Integration.java  # AE2集成
│   │   └── refinedstorage/
│   │       └── RSIntegration.java   # RS集成
│   ├── init/
│   │   ├── ModBlocks.java           # 方块注册
│   │   ├── ModItems.java            # 物品注册
│   │   ├── ModBlockEntities.java    # 方块实体注册
│   │   ├── ModMenuTypes.java        # 菜单注册
│   │   └── ModCreativeTabs.java     # 创造标签页
│   ├── menu/
│   │   └── CoreMenu.java            # 菜单容器
│   ├── network/
│   │   ├── NetworkHandler.java      # 网络处理器
│   │   ├── PacketStorageRequest.java
│   │   ├── PacketStorageSync.java
│   │   ├── PacketExtractItem.java
│   │   └── PacketConfigUpdate.java
│   └── storage/
│       ├── CoreStorage.java         # 世界级存储系统
│       └── CoreConfig.java          # 核心配置
└── src/main/resources/
    ├── META-INF/
    │   └── mods.toml                # 模组元数据
    ├── assets/coremod/
    │   ├── blockstates/
    │   ├── lang/                    # 语言文件
    │   ├── models/
    │   └── textures/
    └── data/coremod/
        └── recipes/                 # 合成配方
```

## 纹理指南

模组需要以下纹理文件：

1. **方块纹理**
   - 路径：`src/main/resources/assets/coremod/textures/block/core_block.png`
   - 尺寸：16x16像素
   - 建议风格：科技/未来主义，参考AE2的驱动器

2. **GUI纹理**
   - 路径：`src/main/resources/assets/coremod/textures/gui/core_gui.png`
   - 尺寸：256x256像素
   - 主界面：176x166像素

详细说明请查看：`src/main/resources/assets/coremod/textures/TEXTURE_GUIDE.txt`

## 待完成功能

- [ ] 完整的AE2网络集成（需要AE2 API依赖）
- [ ] 完整的RS网络集成（需要RS API依赖）
- [ ] 物品自动插入功能（漏斗等）
- [ ] 更好的GUI纹理和视觉效果
- [ ] 2x2多方块结构的完整实现
- [ ] 物品过滤和搜索功能

## 技术细节

### 存储系统

核心使用Minecraft的`SavedData`系统来存储数据：
- 数据保存在世界文件夹的`data`目录中
- 所有核心共享同一个存储映射
- 支持动态容量调整

### 网络同步

使用Forge的网络系统进行客户端-服务器通信：
- `PacketStorageRequest`：请求存储数据
- `PacketStorageSync`：同步存储数据
- `PacketExtractItem`：提取物品
- `PacketConfigUpdate`：更新配置

### 能量系统

实现Forge的`IEnergyStorage`接口：
- 每tick生成100 FE
- 最大缓存：1,000,000 FE
- 只能提取，不能接收

## 许可证

保留所有权利 (All rights reserved)

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

如有问题，请提交Issue。
