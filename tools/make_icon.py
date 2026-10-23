from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter
import math

OUT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"

S = 1024
img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# Deep-space rounded square
pad = 0
d.rounded_rectangle((pad, pad, S - pad, S - pad), radius=210, fill=(6, 9, 14, 255))

# Soft cyan aura
aura = Image.new("RGBA", (S, S), (0, 0, 0, 0))
ad = ImageDraw.Draw(aura)
for i in range(18, 0, -1):
    alpha = int(10 * (18 - i) / 18)
    r = 190 + i * 15
    ad.ellipse((S / 2 - r, S / 2 - r + 20, S / 2 + r, S / 2 + r + 20),
               outline=(98, 208, 255, alpha), width=8)
aura = aura.filter(ImageFilter.GaussianBlur(16))
img.alpha_composite(aura)

# Circuit loop
loop = (222, 266, 802, 758)
d.rounded_rectangle(loop, radius=56, outline=(72, 118, 154, 255), width=18)
d.rounded_rectangle(loop, radius=56, outline=(111, 213, 255, 90), width=7)

# Nodes / terminals
for x, y in [(222, 512), (802, 512), (512, 266), (512, 758)]:
    d.ellipse((x - 34, y - 34, x + 34, y + 34), fill=(10, 17, 26, 255),
              outline=(106, 208, 250, 255), width=10)
    d.ellipse((x - 12, y - 12, x + 12, y + 12), fill=(164, 234, 255, 255))

# Battery symbol on bottom rail
d.rounded_rectangle((400, 728, 624, 788), radius=30, fill=(20, 31, 44, 255),
                    outline=(80, 130, 166, 255), width=6)
for i, x in enumerate((462, 512, 562)):
    h = 42 if i != 1 else 55
    w = 12 if i != 1 else 22
    d.rounded_rectangle((x - w / 2, 758 - h / 2, x + w / 2, 758 + h / 2),
                        radius=6, fill=(111, 213, 255, 255))

# Lightning bolt with glow
bolt = [(548, 150), (388, 522), (492, 522), (446, 728), (654, 378),
        (538, 378), (600, 150)]

glow = Image.new("RGBA", (S, S), (0, 0, 0, 0))
gd = ImageDraw.Draw(glow)
gd.polygon(bolt, fill=(92, 206, 255, 255))
glow = glow.filter(ImageFilter.GaussianBlur(38))
img.alpha_composite(glow)

# Bolt gradient by masking
bolt_layer = Image.new("RGBA", (S, S), (0, 0, 0, 0))
bd = ImageDraw.Draw(bolt_layer)
bd.polygon(bolt, fill=(255, 255, 255, 255))
mask = bolt_layer.split()[3]

grad = Image.new("RGBA", (S, S), (0, 0, 0, 0))
gpx = grad.load()
for y in range(S):
    t = y / S
    r = int(178 + t * 30)
    g = int(240 - t * 25)
    b = 255
    for x in range(S):
        gpx[x, y] = (r, g, b, 255)
grad.putalpha(mask)
img.alpha_composite(grad)

# Inner highlight of bolt
hi = Image.new("RGBA", (S, S), (0, 0, 0, 0))
hd = ImageDraw.Draw(hi)
hd.line([(520, 190), (430, 470), (510, 470), (474, 640)], fill=(255, 255, 255, 150), width=12, joint="curve")
hi = hi.filter(ImageFilter.GaussianBlur(3))
img.alpha_composite(hi)

# Rim light
rim = Image.new("RGBA", (S, S), (0, 0, 0, 0))
rd = ImageDraw.Draw(rim)
rd.rounded_rectangle((6, 6, S - 6, S - 6), radius=206, outline=(150, 230, 255, 112), width=5)
rd.arc((22, 22, S - 22, S - 22), 200, 320, fill=(220, 250, 255, 190), width=8)
rim = rim.filter(ImageFilter.GaussianBlur(1))
img.alpha_composite(rim)

# Save full-res and mipmaps
img.save(Path(__file__).resolve().parent / "icon-1024.png")
sizes = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
for name, size in sizes.items():
    target = OUT / f"mipmap-{name}" / "ic_launcher.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    img.resize((size, size), Image.LANCZOS).save(target)

# Adaptive-icon foreground: keep the luminous circuit art, drop the dark tile and rim.
fg = Image.new("RGBA", (S, S), (0, 0, 0, 0))
src = img.load()
dst = fg.load()
cx = cy = S / 2
for y in range(S):
    for x in range(S):
        r, g, b, a = src[x, y]
        if (x - cx) ** 2 + (y - cy) ** 2 > 436 ** 2:
            continue
        brightness = max(r, g, b)
        if brightness < 55:
            continue
        alpha = min(255, int((brightness - 48) * 2.4))
        dst[x, y] = (r, g, b, alpha)
fg.save(Path(__file__).resolve().parent / "icon-foreground-1024.png")
adaptive = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
for name, size in adaptive.items():
    target = OUT / f"mipmap-{name}" / "ic_launcher_foreground.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    fg.resize((size, size), Image.LANCZOS).save(target)

print("icons written")
