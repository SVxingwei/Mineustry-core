#!/usr/bin/env python3
"""
生成核心模组的占位符纹理
需要安装PIL: pip install Pillow
"""

try:
    from PIL import Image, ImageDraw, ImageFont
    import os
except ImportError:
    print("错误：需要安装Pillow库")
    print("运行: pip install Pillow")
    exit(1)

def create_block_texture():
    """创建16x16的方块纹理"""
    # 创建一个16x16的图像
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 绘制深蓝色背景
    draw.rectangle([0, 0, 15, 15], fill=(20, 40, 80, 255))
    
    # 绘制青色边框
    draw.rectangle([0, 0, 15, 15], outline=(0, 200, 255, 255), width=1)
    
    # 绘制内部装饰
    draw.rectangle([2, 2, 13, 13], outline=(100, 150, 255, 255), width=1)
    
    # 绘制中心点（发光效果）
    draw.rectangle([6, 6, 9, 9], fill=(0, 255, 255, 255))
    draw.rectangle([7, 7, 8, 8], fill=(255, 255, 255, 255))
    
    # 绘制角落装饰
    for x, y in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        draw.point((x, y), fill=(0, 255, 255, 200))
    
    return img

def create_gui_texture():
    """创建256x256的GUI纹理"""
    # 创建一个256x256的图像
    img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 主GUI背景 (176x166)
    # 深色背景
    draw.rectangle([0, 0, 175, 165], fill=(50, 50, 60, 255))
    
    # 边框
    draw.rectangle([0, 0, 175, 165], outline=(120, 120, 140, 255), width=2)
    
    # 内边框
    draw.rectangle([2, 2, 173, 163], outline=(80, 80, 100, 255), width=1)
    
    # 合成网格区域装饰（3x3）
    # 绘制合成网格背景
    for row in range(3):
        for col in range(3):
            x = 30 + col * 18
            y = 17 + row * 18
            draw.rectangle([x-1, y-1, x+16, y+16], outline=(100, 100, 120, 255), width=1)
    
    # 合成结果槽位装饰
    draw.rectangle([123, 34, 140, 51], outline=(100, 255, 100, 255), width=2)
    
    # 箭头（指向合成结果）
    draw.polygon([(90, 39), (90, 45), (100, 42)], fill=(100, 255, 100, 255))
    
    # 玩家背包区域装饰
    draw.rectangle([7, 83, 169, 141], outline=(80, 80, 100, 150), width=1)
    draw.rectangle([7, 141, 169, 159], outline=(80, 80, 100, 150), width=1)
    
    # 槽位装饰（在坐标176, 16开始）
    # 单个槽位背景 (18x18)
    draw.rectangle([176, 16, 193, 33], fill=(40, 40, 50, 255))
    draw.rectangle([176, 16, 193, 33], outline=(100, 100, 120, 255), width=1)
    
    # 滚动条（在坐标176, 34开始）
    # 滚动条背景 (12x54)
    draw.rectangle([176, 34, 187, 87], fill=(60, 60, 70, 255))
    draw.rectangle([176, 34, 187, 87], outline=(100, 100, 120, 255), width=1)
    
    # 滚动条滑块
    draw.rectangle([177, 35, 186, 45], fill=(120, 120, 140, 255))
    
    return img

def main():
    # 创建纹理目录
    block_texture_dir = "src/main/resources/assets/coremod/textures/block"
    gui_texture_dir = "src/main/resources/assets/coremod/textures/gui"
    
    os.makedirs(block_texture_dir, exist_ok=True)
    os.makedirs(gui_texture_dir, exist_ok=True)
    
    # 生成方块纹理
    print("生成方块纹理...")
    block_texture = create_block_texture()
    block_texture.save(os.path.join(block_texture_dir, "core_block.png"))
    print(f"✓ 方块纹理已保存到: {block_texture_dir}/core_block.png")
    
    # 生成GUI纹理
    print("生成GUI纹理...")
    gui_texture = create_gui_texture()
    gui_texture.save(os.path.join(gui_texture_dir, "core_gui.png"))
    print(f"✓ GUI纹理已保存到: {gui_texture_dir}/core_gui.png")
    
    print("\n纹理生成完成！")
    print("注意：这些是基础的占位符纹理。")
    print("建议使用专业的像素画编辑器（如Aseprite、GIMP）来创建更美观的纹理。")

if __name__ == "__main__":
    main()
