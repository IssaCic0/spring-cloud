import argparse
import json
import os
from pathlib import Path
from datetime import datetime
import sys

# Ensure project root is on sys.path so we can import pdf_parser when running from tools/
_CURRENT = Path(__file__).resolve()
_PROJECT_ROOT = _CURRENT.parent.parent
if str(_PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(_PROJECT_ROOT))

from pdf_parser import PdfParser, logger

INVALID_CHARS = '<>:"/\\|?*'


def sanitize_name(name: str) -> str:
    for ch in INVALID_CHARS:
        name = name.replace(ch, '_')
    return name.strip()


def find_pdfs(root: Path):
    for path in root.rglob('*.pdf'):
        if path.is_file():
            yield path


def as_bool(v) -> bool:
    return str(v).lower() in ("1", "true", "yes", "y", "on")


def main():
    parser = argparse.ArgumentParser(description="Batch parse PDFs in a directory")
    parser.add_argument("--root", required=True, help="Root directory to search PDFs")
    parser.add_argument("--out", required=True, help="Output directory for parsed content")
    parser.add_argument("--images", default="true", help="Whether to extract images: true/false")
    parser.add_argument("--merge-text", dest="merge_text", default="true", help="Whether to merge texts into one file: true/false")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    out_dir = Path(args.out).resolve()
    out_dir.mkdir(parents=True, exist_ok=True)

    extract_images = as_bool(args.images)
    merge_text = as_bool(args.merge_text)

    items = []
    merged_text_lines = []

    logger.info(f"开始批量解析：root={root}, out={out_dir}, images={extract_images}, merge_text={merge_text}")

    for pdf_path in find_pdfs(root):
        try:
            rel = pdf_path.relative_to(root)
        except Exception:
            rel = pdf_path.name

        stem = sanitize_name(pdf_path.stem)
        subdir = out_dir / stem
        subdir.mkdir(parents=True, exist_ok=True)

        try:
            with PdfParser(str(pdf_path), str(subdir)) as parser_obj:
                text_content = parser_obj.extract_text()
                text_file = subdir / '题目.txt'
                text_file.write_text(text_content, encoding='utf-8')

                images = []
                if extract_images:
                    images = parser_obj.extract_images(prefix='题目图片')

                pages = len(parser_obj.doc)
                image_count = len(images)

            item = {
                "pdf": str(pdf_path),
                "relative": str(rel),
                "out_dir": str(subdir),
                "text_file": str(text_file),
                "pages": pages,
                "image_count": image_count,
                "chars": len(text_content)
            }
            items.append(item)

            if merge_text:
                merged_text_lines.append(f"=== {rel} ===")
                merged_text_lines.append(text_content)
                merged_text_lines.append("")

            logger.info(f"完成：{rel} pages={pages} images={image_count} chars={len(text_content)}")
        except Exception as e:
            logger.error(f"解析失败：{pdf_path} -> {e}")

    index_obj = {
        "root": str(root),
        "out": str(out_dir),
        "count": len(items),
        "generated_at": datetime.now().isoformat(),
        "items": items,
    }

    (out_dir / 'index.json').write_text(json.dumps(index_obj, ensure_ascii=False, indent=2), encoding='utf-8')

    if merge_text:
        (out_dir / 'all_texts.txt').write_text('\n'.join(merged_text_lines), encoding='utf-8')

    print(f"Processed {len(items)} PDFs. Index at {(out_dir / 'index.json')}.")


if __name__ == "__main__":
    main()
