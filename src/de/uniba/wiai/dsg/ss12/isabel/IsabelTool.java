package de.uniba.wiai.dsg.ss12.isabel;

/*
 *#####################################################################
 *                      #                                             #
 *            ;@,       #              I S A B E L                    #
 *     ,        *@,     #                                             #
 *   ;#     ,@@,  *@    #(I statically  analyze BPEL-files flawlessly)#
 *  #*      *@#*  ,@    #                                             #
 *  #            @&     #                                             #
 *  #,     ,@@,   '@    #                                             #
 *   *#    *@#*   ,@    #                                             #
 *    ^        ,,@*     #                                             #
 *            ***       #                                             #
 *                      #                                             #
 *#####################################################################
 */

import de.uniba.wiai.dsg.ss12.isabel.tool.Isabel;
import de.uniba.wiai.dsg.ss12.isabel.tool.ValidationException;
import de.uniba.wiai.dsg.ss12.isabel.tool.reports.ValidationResult;

public class IsabelTool {

	public static void main(String[] args) {
		ValidationResultPrinter validationResultPrinter = new ValidationResultPrinter();
		try {
			CommandLineInterpreter input = new CommandLineInterpreter(args);
			ValidationResult validationResult = new Isabel().validate(input.bpel_file);

			validationResultPrinter.printResults(input.verbosityLevel, validationResult);
		} catch (ValidationException | IllegalArgumentException e) {
			validationResultPrinter.printStartUpError(e);
		}
	}
}