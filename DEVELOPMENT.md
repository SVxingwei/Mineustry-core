# 开发指南

## 项目结构

本模组使用标准的Forge 1.20.1模组结构，采用现代化的架构设计。

## 核心系统

### 1. 存储系统（CoreStorage）

**位置：** `com.coremod.storage.CoreStorage`

**职责：**
- 管理所有核心的存储数据
- 保存和加载世界数据
- 处理物品插入和提取
- 动态容量管理

**关键方法：**
```java
// 获取存储实例
CoreStorage.get(ServerLevel level)

// 注册核心
registerCore(String coreId)

// 插入物品
insertItem(String coreId, ItemStack stack, boolean simulate)

// 提取物品
extractItem(String coreId, String itemKey, int amount, boolean simulate)
```

**数据持久化：**
- 使用Minecraft的SavedData系统
- 数据保存在世界文件夹的`data`目录
- 文件名：`coremod_storage.dat`
- 格式：NBT

### 2. 配置系统（CoreConfig）

**位置：** `com.coremod.storage.CoreConfig`

**配置项：**
- `coreId`: 核心标识符
- `isPublic`: 是否为公共核心
- `ownerId`: 拥有者UUID
- `allowOthers`: 是否允许其他玩家
- `burnOverflow`: 是否烧毁溢出物品

**存储位置：**
- 保存在核心方块实体的NBT中
- 每个核心独立配置

### 3. 网络系统

**位置：** `com.coremod.network`

**架构：**
```
客户端 ←→ 服务器
   ↓         ↓
Packet ←→ Handler
```

**包类型：**

1. **PacketStorageRequest（C→S）**
   - 客户端请求存储数据
   - 触发时机：打开GUI

2. **PacketStorageSync（S→C）**
   - 服务器同步存储数据
   - 包含所有物品信息

3. **PacketExtractItem（C→S）**
   - 客户端请求提取物品
   - 包含物品键和数量

4. **PacketConfigUpdate（C→S）**
   - 客户端更新配置
   - 包含所有配置选项

### 4. GUI系统

**架构：**
```
Screen (客户端)
   ↓
Menu (双端)
   ↓
BlockEntity (服务器)
```

**CoreMenu：**
- 管理槽位
- 处理合成
- 快速移动物品

**CoreScreen：**
- 渲染GUI
- 处理用户输入
- 切换视图（合成/浏览）

**CoreConfigScreen：**
- 配置界面
- 表单验证
- 网络通信

## 开发工作流

### 添加新功能

1. **添加新的存储功能：**
```java
// 1. 在CoreStorage中添加方法
public void newFeature(String coreId, ...) {
    // 实现逻辑
    setDirty(); // 标记为需要保存
}

// 2. 在CoreMenu中暴露功能
public void callNewFeature() {
    CoreStorage storage = blockEntity.getStorage();
    storage.newFeature(...);
}

// 3. 在GUI中调用
// CoreScreen.java
button.onClick(() -> {
    menu.callNewFeature();
});
```

2. **添加新的网络包：**
```java
// 1. 创建包类
public class PacketNewFeature {
    private final Data data;
    
    public PacketNewFeature(Data data) {
        this.data = data;
    }
    
    public PacketNewFeature(FriendlyByteBuf buf) {
        this.data = buf.read...();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.write...(data);
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 处理逻辑
        });
        ctx.get().setPacketHandled(true);
    }
}

// 2. 在NetworkHandler中注册
INSTANCE.messageBuilder(PacketNewFeature.class, id(), NetworkDirection.PLAY_TO_SERVER)
    .decoder(PacketNewFeature::new)
    .encoder(PacketNewFeature::toBytes)
    .consumerMainThread(PacketNewFeature::handle)
    .add();
```

3. **添加新的GUI组件：**
```java
// 在CoreScreen.init()中
this.addRenderableWidget(new CustomWidget(
    x, y, width, height,
    Component.literal("Text"),
    button -> {
        // 点击处理
    }
));
```

### 调试技巧

**日志输出：**
```java
CoreMod.LOGGER.info("Message: {}", value);
CoreMod.LOGGER.warn("Warning!");
CoreMod.LOGGER.error("Error!", exception);
```

**NBT调试：**
```java
CompoundTag tag = stack.save(new CompoundTag());
CoreMod.LOGGER.info("NBT: {}", tag);
```

**网络调试：**
```java
// 在包处理器中
CoreMod.LOGGER.info("Received packet: {}", this.getClass().getSimpleName());
```

## 测试

### 本地测试

1. **启动测试客户端：**
```bash
./gradlew runClient
```

2. **启动测试服务器：**
```bash
./gradlew runServer
```

### 测试场景

**场景1：基础存储测试**
```
1. 创建新世界
2. 放置核心方块
3. 打开GUI
4. 存入物品
5. 提取物品
6. 验证数量正确
```

**场景2：容量测试**
```
1. 放置1个核心
2. 存入8000个物品
3. 尝试存入更多（应失败）
4. 放置第2个核心（相同ID）
5. 现在可以存入16000个
```

**场景3：共享测试**
```
1. 玩家A放置核心（ID="test"）
2. 玩家A存入物品
3. 玩家B放置核心（ID="test"，公共模式）
4. 玩家B可以看到玩家A的物品
```

## 性能分析

### 使用JProfiler/VisualVM

1. 连接到游戏进程
2. 关注以下热点：
   - `CoreStorage.insertItem()`
   - `CoreStorage.save()`
   - GUI渲染方法

### 优化建议

**存储优化：**
- 使用HashMap而非List存储物品
- 延迟保存（批量写入）
- 增量更新而非完整重载

**网络优化：**
- 只发送变化的数据
- 压缩大型数据包
- 使用缓存减少请求

**渲染优化：**
- 虚拟滚动大列表
- 缓存ItemStack渲染
- 使用批量渲染

## 与其他模组集成

### AE2集成步骤

1. **添加依赖：**
```gradle
// build.gradle
repositories {
    maven {
        name = "AppliedEnergistics"
        url = "https://modmaven.dev/"
    }
}

dependencies {
    implementation fg.deobf("appeng:appliedenergistics2:VERSION")
}
```

2. **实现接口：**
```java
public class CoreBlockEntity extends BlockEntity implements IGridHost {
    private IGridNode gridNode;
    
    @Override
    public IGridNode getGridNode(Direction dir) {
        return gridNode;
    }
    
    // ... 其他实现
}
```

### 通用模组兼容性

**能量系统：**
- Forge Energy (FE) - ✅ 已实现
- RF (Redstone Flux) - ✅ 兼容FE
- EU (Industrial Credits) - ❌ 未实现

**物品系统：**
- IItemHandler - ⚠️ 需实现
- IInventory - ❌ 已过时

## 发布清单

- [ ] 所有功能测试通过
- [ ] 没有编译警告
- [ ] README更新
- [ ] 版本号更新
- [ ] 变更日志编写
- [ ] 纹理文件就位
- [ ] 构建JAR文件
- [ ] 测试JAR文件
- [ ] 创建GitHub Release
- [ ] 上传到CurseForge
- [ ] 上传到Modrinth

## 获取帮助

- **Forge文档：** https://docs.minecraftforge.net/
- **Forge论坛：** https://forums.minecraftforge.net/
- **Discord：** MinecraftForge服务器
- **GitHub Issues：** 提交bug报告

## 贡献指南

1. Fork仓库
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

**代码风格：**
- 使用4空格缩进
- 驼峰命名法
- 添加JavaDoc注释
- 遵循现有代码风格
