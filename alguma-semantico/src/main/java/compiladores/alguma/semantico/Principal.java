package compiladores.alguma.semantico;

import compiladores.alguma.semantico.AlgumaParser.ProgramaContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Principal {

    public static void main(String args[]) throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(args[1]))) {
            try {
                CharStream cs = CharStreams.fromFileName(args[0]);
                
                AlgumaLexer lexer = new AlgumaLexer(cs);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                
                AlgumaParser parser = new AlgumaParser(tokens);
                ProgramaContext arvore = parser.programa();
                AlgumaSemantico alguma = new AlgumaSemantico();
                alguma.visitPrograma(arvore);
                
                // Verifica se há erros, exibe todos os erros encontrados e encerra a operação do analisador.
                for(String error: AlgumaSemanticoUtils.errosSemanticos){
                    pw.println(error);
                }
                pw.println("Fim da compilacao");
                pw.close();                
            } catch (RuntimeException e) {
            }
        }
    }
}