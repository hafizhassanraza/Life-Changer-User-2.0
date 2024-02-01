package com.enfotrix.life_changer_user_2_0.Pdf


import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class PdfProfitHistory(var taxList: List<TransactionModel>?, var firstName: String) {
    fun generatePdf(outputStream: OutputStream): Boolean {
        val document = Document()

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream)
            document.open()
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)
            var title: Paragraph
            title = Paragraph("$firstName Earnings ", titleFont)
            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("\n"))
            val table = PdfPTable(3)
            table.widthPercentage = 100f
            val headers = arrayOf(
                Paragraph("New Profit", titleFont),
                Paragraph("Old Profit", titleFont),
                Paragraph("Release Date", titleFont),

                )
            for (header in headers) {
                val cell = PdfPCell(Phrase(header))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(cell)
            }
            for (item in taxList!!) {
                table.addCell(item.newBalance)
                table.addCell(item.previousBalance)
                table.addCell(SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(item.transactionAt!!.toDate()))
            }
            document.add(table)
            document.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
