package nbcp.base.utils

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.IOException
import java.awt.Image.SCALE_AREA_AVERAGING
import java.awt.Transparency
import java.awt.Graphics2D
import nbcp.base.extend.FileExtentionInfo
import nbcp.base.extend.FullName
import java.awt.Image
import java.io.File


/**
 * Created by udi on 17-4-14.
 */

object ImageUtil {

    /**
     * 按指定高度 等比例缩放图片
     * @param imageFile
     * @param newPath
     * @param newWidth 新图的宽度
     * @throws IOException
     */
    fun zoomImageScale(sourceImageFileName: String, destFileName: String, maxWidth: Int,maxHeight:Int): String {
        var destFile = File(destFileName);
        if (destFile.exists() == false) {
            if (destFile.parentFile.exists() == false && destFile.parentFile.mkdirs() == false) {
                return "创建文件夹失败: ${destFile.parentFile.FullName}"
            }
//            if (destFile.createNewFile() == false) {
//                return "创建文件失败 ${destFile.FullName}"
//            }
        }

        var imageFile = File(sourceImageFileName);
        if (!imageFile.canRead())
            return "文件不可读：${imageFile.FullName}"

        val bufferedImage = ImageIO.read(imageFile)

        val originalWidth = bufferedImage.getWidth()
        val originalHeight = bufferedImage.getHeight()
        val width_scale = originalWidth.toDouble() / maxWidth.toDouble()    // 缩放的比例
        val height_scale = originalHeight.toDouble() / maxHeight.toDouble()    // 缩放的比例

        val scale = Math.max(width_scale ,height_scale);
        if( scale <=1 ){
            return "";
        }

        val newWidth = (originalWidth / scale).toInt()
        val newHeight = (originalHeight / scale).toInt()
        return zoomImageUtils(imageFile, destFile, bufferedImage, newWidth, newHeight)
    }

    private fun zoomImageUtils(imageFile: File, destFile: File, bufferedImage: BufferedImage, width: Int, height: Int): String {
        val suffix = FileExtentionInfo(imageFile.getName()).extName;

        // 处理 png 背景变黑的问题
        if (suffix != null && (suffix.trim({ it <= ' ' }).toLowerCase().endsWith("png") || suffix.trim({ it <= ' ' }).toLowerCase().endsWith("gif"))) {
            var to = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            var g2d = to.createGraphics()
            to = g2d.deviceConfiguration.createCompatibleImage(width, height, Transparency.TRANSLUCENT)
            g2d.dispose()

            g2d = to.createGraphics()
            val from = bufferedImage.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING)
            g2d.drawImage(from, 0, 0, null)
            g2d.dispose()

            if (ImageIO.write(to, suffix, destFile) == false) {
                return "写入新文件失败: ${destFile.FullName}"
            }
        } else {
            // 高质量压缩，其实对清晰度而言没有太多的帮助
            //            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            //            tag.getGraphics().drawImage(bufferedImage, 0, 0, width, height, null);
            //
            //            FileOutputStream out = new FileOutputStream(newPath);    // 将图片写入 newPath
            //            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            //            JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(tag);
            //            jep.setQuality(1f, true);    //压缩质量, 1 是最高值
            //            encoder.encode(tag, jep);
            //            out.close();

            val newImage = BufferedImage(width, height, bufferedImage.type)
            val g = newImage.graphics
            g.drawImage(bufferedImage, 0, 0, width, height, null)
            g.dispose()
            if (ImageIO.write(newImage, suffix, destFile) == false) {
                return "写入新文件失败: ${destFile.FullName}"
            }
        }

        return ""
    }
}