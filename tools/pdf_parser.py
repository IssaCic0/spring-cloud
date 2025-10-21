"""PDF 解析工具 - 提取文本和图片"""
import os
import logging
import fitz  # PyMuPDF
from PIL import Image
from io import BytesIO
try:
    from app.utils.log_util import logger
except Exception:
    logger = logging.getLogger("pdf_parser")
    if not logger.handlers:
        _handler = logging.StreamHandler()
        _handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s [%(name)s] %(message)s"))
        logger.addHandler(_handler)
    logger.setLevel(logging.INFO)


class PdfParser:
    """PDF 文件解析器"""
    
    def __init__(self, pdf_path: str, output_dir: str):
        """
        :param pdf_path: PDF 文件路径
        :param output_dir: 输出目录（保存提取的图片）
        """
        self.pdf_path = pdf_path
        self.output_dir = output_dir
        self.doc = None
    
    def __enter__(self):
        self.doc = fitz.open(self.pdf_path)
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        if self.doc:
            self.doc.close()
    
    def extract_text(self) -> str:
        """
        提取 PDF 中的所有文本内容
        :return: 提取的文本
        """
        if not self.doc:
            raise ValueError("PDF 文档未打开，请使用 with 语句")
        
        full_text = []
        
        for page_num in range(len(self.doc)):
            page = self.doc[page_num]
            text = page.get_text()
            
            if text.strip():
                full_text.append(f"=== 第 {page_num + 1} 页 ===\n")
                full_text.append(text)
                full_text.append("\n")
        
        logger.info(f"从 PDF 中提取了 {len(self.doc)} 页文本")
        return "\n".join(full_text)
    
    def extract_images(self, prefix: str = "pdf_image") -> list[str]:
        """
        提取 PDF 中的所有图片
        :param prefix: 图片文件名前缀
        :return: 提取的图片文件路径列表
        """
        if not self.doc:
            raise ValueError("PDF 文档未打开，请使用 with 语句")
        
        os.makedirs(self.output_dir, exist_ok=True)
        
        extracted_images = []
        image_count = 0
        
        for page_num in range(len(self.doc)):
            page = self.doc[page_num]
            image_list = page.get_images(full=True)
            
            for img_index, img in enumerate(image_list):
                try:
                    xref = img[0]
                    base_image = self.doc.extract_image(xref)
                    image_bytes = base_image["image"]
                    image_ext = base_image["ext"]
                    
                    # 保存图片
                    image_filename = f"{prefix}_page{page_num + 1}_{img_index + 1}.{image_ext}"
                    image_path = os.path.join(self.output_dir, image_filename)
                    
                    with open(image_path, "wb") as img_file:
                        img_file.write(image_bytes)
                    
                    extracted_images.append(image_filename)
                    image_count += 1
                    logger.info(f"提取图片: {image_filename}")
                    
                except Exception as e:
                    logger.warning(f"提取图片失败（页 {page_num + 1}，图 {img_index + 1}）: {str(e)}")
                    continue
        
        logger.info(f"从 PDF 中提取了 {image_count} 张图片")
        return extracted_images
    
    def extract_all(self, text_filename: str = "题目.txt", image_prefix: str = "题目图片") -> dict:
        """
        提取 PDF 中的所有内容（文本和图片）
        :param text_filename: 文本文件名
        :param image_prefix: 图片前缀
        :return: {"text_file": str, "images": list[str], "text_content": str}
        """
        # 提取文本
        text_content = self.extract_text()
        
        # 保存文本到文件
        text_path = os.path.join(self.output_dir, text_filename)
        with open(text_path, 'w', encoding='utf-8') as f:
            f.write(text_content)
        
        # 提取图片
        images = self.extract_images(prefix=image_prefix)
        
        return {
            "text_file": text_filename,
            "text_content": text_content,
            "images": images,
            "image_count": len(images)
        }


def parse_pdf_question(pdf_path: str, work_dir: str) -> str:
    """
    解析 PDF 题目文件
    :param pdf_path: PDF 文件路径
    :param work_dir: 工作目录
    :return: 提取的文本内容
    """
    try:
        with PdfParser(pdf_path, work_dir) as parser:
            result = parser.extract_all()
            
            logger.info(f"PDF 解析完成：")
            logger.info(f"  - 文本文件: {result['text_file']}")
            logger.info(f"  - 提取图片: {result['image_count']} 张")
            
            return result['text_content']
            
    except Exception as e:
        logger.error(f"解析 PDF 失败: {str(e)}")
        raise

