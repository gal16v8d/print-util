package com.gsdd.print.util;

import com.gsdd.constants.PrintConstants;
import com.gsdd.exception.TechnicalException;
import com.gsdd.validatorutil.ValidatorUtil;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.Arrays;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Great System Development Dynamic (<b>GSDD</b>) <br>
 *     Alexander Galvis Grisales <br>
 *     alex.galvis.sistemas@gmail.com <br>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrintUtil {

  public boolean print(Printable printableObject, String printerName) {
    try {
      PrintService service = getPrintService(printerName);
      PrinterJob job = PrinterJob.getPrinterJob();
      job.setPrintService(service);
      PageFormat pf = job.defaultPage();
      Paper paper = new Paper();
      paper.setSize(200, paper.getHeight());
      double margin = 1;
      paper.setImageableArea(margin, margin, paper.getWidth() - margin, paper.getHeight() - margin);
      pf.setPaper(paper);
      pf.setOrientation(PageFormat.PORTRAIT);
      job.setPrintable(printableObject, pf);
      job.print();
      return true;
    } catch (TechnicalException e) {
      throw e;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  private PrintService getPrintService(String printerName) {
    if (!ValidatorUtil.isNullOrEmpty(printerName)) {
      AttributeSet attributeSet = new HashAttributeSet();
      attributeSet.add(new PrinterName(printerName, null));
      PrintService[] services = PrintServiceLookup.lookupPrintServices(null, attributeSet);
      return validatePrintService(services, false);
    } else {
      return validatePrintService();
    }
  }

  private PrintService validatePrintService() {
    PrintService def = PrintServiceLookup.lookupDefaultPrintService();
    if (def == null) {
      PrintService[] serv = PrintServiceLookup.lookupPrintServices(null, null);
      log.info("Print services: {}", Arrays.toString(serv));
      return validatePrintService(serv, true);
    } else {
      return def;
    }
  }

  private PrintService validatePrintService(PrintService[] serv, boolean def) {
    if (serv.length == 0 && def) {
      throw new TechnicalException(PrintConstants.NO_PRINT_SERVICE);
    } else if (serv.length == 0 && !def) {
      return validatePrintService();
    } else {
      // check for avoid to take virtual unit as printer
      for (PrintService p : serv) {
        PrintServiceAttributeSet psa = p.getAttributes();
        String pName = p.getName();
        String aName = psa.get(PrinterName.class).toString();
        if (!(aName.indexOf(PrintConstants.REDIRECTED) != -1
            || pName.indexOf(PrintConstants.REDIRECTED) != -1)) {
          return p;
        }
      }

      throw new TechnicalException(PrintConstants.NO_PRINT_SERVICE);
    }
  }
}
