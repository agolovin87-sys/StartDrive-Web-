from __future__ import annotations

from collections import deque
import os
from typing import Iterable, Tuple

from PIL import Image, ImageFilter


def _avg_rgba(pixels: Iterable[Tuple[int, int, int, int]]) -> Tuple[int, int, int, int]:
    px = list(pixels)
    if not px:
        return (0, 0, 0, 0)
    return tuple(int(sum(c[i] for c in px) / len(px)) for i in range(4))  # type: ignore[return-value]


def _color_dist_sq(a: Tuple[int, int, int, int], b: Tuple[int, int, int, int]) -> int:
    return (a[0] - b[0]) ** 2 + (a[1] - b[1]) ** 2 + (a[2] - b[2]) ** 2


def _make_edge_background_transparent(path: str, *, dist_threshold: int = 55, blur_radius: float = 1.2) -> None:
    img = Image.open(path).convert("RGBA")
    w, h = img.size
    px = img.load()

    # Pick background color from corners (usually solid black here).
    corners = [
        px[0, 0],
        px[w - 1, 0],
        px[0, h - 1],
        px[w - 1, h - 1],
    ]
    bg = _avg_rgba(corners)
    thr_sq = dist_threshold * dist_threshold

    visited = bytearray(w * h)

    def idx(x: int, y: int) -> int:
        return y * w + x

    def is_bg(x: int, y: int) -> bool:
        r, g, b, a = px[x, y]
        if a < 8:
            return True
        return _color_dist_sq((r, g, b, a), bg) <= thr_sq and r < 40 and g < 40 and b < 40

    q: deque[tuple[int, int]] = deque()
    mask = Image.new("L", (w, h), 0)
    mpx = mask.load()

    def push(x: int, y: int) -> None:
        i = idx(x, y)
        if visited[i]:
            return
        if not is_bg(x, y):
            return
        visited[i] = 1
        mpx[x, y] = 255
        q.append((x, y))

    for x in range(w):
        push(x, 0)
        push(x, h - 1)
    for y in range(h):
        push(0, y)
        push(w - 1, y)

    while q:
        x, y = q.popleft()
        if x > 0:
            push(x - 1, y)
        if x + 1 < w:
            push(x + 1, y)
        if y > 0:
            push(x, y - 1)
        if y + 1 < h:
            push(x, y + 1)

    # Feather edge slightly for nicer anti-aliasing.
    if blur_radius > 0:
        mask = mask.filter(ImageFilter.GaussianBlur(radius=blur_radius))

    alpha = img.getchannel("A")
    a_data = list(alpha.getdata())
    m_data = list(mask.getdata())
    new_a = [int(a * (255 - m) / 255) for a, m in zip(a_data, m_data)]
    alpha2 = Image.new("L", (w, h))
    alpha2.putdata(new_a)
    img.putalpha(alpha2)

    img.save(path, format="PNG", optimize=True)


def main() -> None:
    drawable = r"c:\StartDrive\app\src\main\res\drawable"
    icons = [
        os.path.join(drawable, "ic_home_instructor.png"),
        os.path.join(drawable, "ic_record_instructor.png"),
        os.path.join(drawable, "ic_chat_instructor.png"),
        os.path.join(drawable, "ic_history_instructor.png"),
    ]
    for p in icons:
        _make_edge_background_transparent(p)
        print("OK", os.path.basename(p))


if __name__ == "__main__":
    main()

