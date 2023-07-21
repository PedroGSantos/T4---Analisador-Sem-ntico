package compiladores.alguma.semantico;

import compiladores.alguma.semantico.AlgumaParser.TermoContext;
import compiladores.alguma.semantico.TabelaDeSimbolos.TipoAlguma;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

public class AlgumaSemanticoUtils {

    // Lista que armazena erros identificados pelo analisador.
    public static List<String> errosSemanticos = new ArrayList<>();

    // Adiciona um novo erro identificado à lista.
    public static void adicionaErroSemantico(Token tok, String mensagem) {
        int linha = tok.getLine();
        if (!errosSemanticos.contains("Linha " + linha + ": " + mensagem)) 
            errosSemanticos.add(String.format("Linha %d: %s", linha, mensagem));
    }
    
    // Verifica a compatibilidade entre operadores aritméticos, se a operação envolver 
    // um valor real, mesmo que um seja inteiro, trata como uma operação entre reais.
    public static boolean verificaCompatibilidade(TipoAlguma T1, TipoAlguma T2) {
        boolean flag = false;
        
        if (T1 == TipoAlguma.INTEIRO && T2 == TipoAlguma.REAL)
            flag = true;
        else if (T1 == TipoAlguma.REAL && T2 == TipoAlguma.INTEIRO)
            flag = true;
        else if (T1 == TipoAlguma.REAL && T2 == TipoAlguma.REAL)
            flag = true;
        
        return flag;
    }
    
    // Verifica a compatibilidade entre operadores para tratá-los como uma operação lógica.
    public static boolean verificaCompatibilidadeLogica(TipoAlguma T1, TipoAlguma T2) {
        boolean flag = false;
        
        if (T1 == TipoAlguma.INTEIRO && T2 == TipoAlguma.REAL)
            flag = true;
        else if (T1 == TipoAlguma.REAL && T2 == TipoAlguma.INTEIRO)
            flag = true;

        return flag;
    }
    
    // Verifica o tipo de uma expressão aritmética 
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.Exp_aritmeticaContext ctx) {
        TipoAlguma tipoRetorno = verificarTipo(tabela, ctx.termo().get(0));

        for (TermoContext termoArit : ctx.termo()) {
            TipoAlguma tipoAtual = verificarTipo(tabela, termoArit);
            
            if ((verificaCompatibilidade(tipoAtual, tipoRetorno)) && (tipoAtual != TipoAlguma.INVALIDO))
                tipoRetorno = TipoAlguma.REAL;
            else
                tipoRetorno = tipoAtual;
        }

        return tipoRetorno;
    }

    // Verifica o tipo de um termo.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.TermoContext ctx) {
        TipoAlguma tipoRetorno = verificarTipo(tabela, ctx.fator().get(0));
                
        for (AlgumaParser.FatorContext fatorArit : ctx.fator()) {
            TipoAlguma tipoAtual = verificarTipo(tabela, fatorArit);
            
            if ((verificaCompatibilidade(tipoAtual, tipoRetorno)) && (tipoAtual != TipoAlguma.INVALIDO))
                tipoRetorno = TipoAlguma.REAL;
            else
                tipoRetorno = tipoAtual;
        }
        
        return tipoRetorno;
    }

    // Verifica o tipo de um fator.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.FatorContext ctx) {
        TipoAlguma tipoRetorno = null;
        
        for (AlgumaParser.ParcelaContext parcela : ctx.parcela())
            tipoRetorno = verificarTipo(tabela, parcela);

        return tipoRetorno;
    }

    // Verifica o tipo de uma parcela.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.ParcelaContext ctx) {
        if (ctx.parcela_unario() != null)
            return verificarTipo(tabela, ctx.parcela_unario());
        else
            return verificarTipo(tabela, ctx.parcela_nao_unario());
    }

    // Verifica o tipo de uma parcela unária.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.Parcela_unarioContext ctx) {
        TipoAlguma tipoRetorno;
        String nome;
        
        if (ctx.identificador() != null) {
            nome = ctx.identificador().getText();
            
            if (tabela.existe(nome))
                tipoRetorno = tabela.verificar(nome);
            
            else {
                TabelaDeSimbolos tabelaAux = AlgumaSemantico.escoposAninhados.percorrerEscoposAninhados().get(AlgumaSemantico.escoposAninhados.percorrerEscoposAninhados().size() - 1);
                if (!tabelaAux.existe(nome)) {
                    adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
                    tipoRetorno = TipoAlguma.INVALIDO;
                } else 
                    tipoRetorno = tabelaAux.verificar(nome);
            }
        } else if (ctx.NUM_INT() != null)
            tipoRetorno = TipoAlguma.INTEIRO;
        else if (ctx.NUM_REAL() != null)
            tipoRetorno = TipoAlguma.REAL;
        else
            tipoRetorno = verificarTipo(tabela, ctx.exp_aritmetica().get(0));

        return tipoRetorno;
    }

    // Verifica o tipo de uma parcela não unária.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.Parcela_nao_unarioContext ctx) {
        TipoAlguma tipoRetorno;
        String nome;

        if (ctx.identificador() != null) {
            nome = ctx.identificador().getText();
        
            if (!tabela.existe(nome)) {
                adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
                tipoRetorno = TipoAlguma.INVALIDO;
            } else 
                tipoRetorno = tabela.verificar(ctx.identificador().getText());
        } else
            tipoRetorno = TipoAlguma.LITERAL;

        return tipoRetorno;
    }

    // Verifica o tipo de uma expressão lógica.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.ExpressaoContext ctx) {
        TipoAlguma tipoRetorno = verificarTipo(tabela, ctx.termo_logico(0));

        for (AlgumaParser.Termo_logicoContext termoLogico : ctx.termo_logico()) {
            TipoAlguma tipoAtual = verificarTipo(tabela, termoLogico);
            if (tipoRetorno != tipoAtual && tipoAtual != TipoAlguma.INVALIDO)
                tipoRetorno = TipoAlguma.INVALIDO;
        }

        return tipoRetorno;
    }

    // Verifica o tipo de um termo lógico.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.Termo_logicoContext ctx) {
        TipoAlguma tipoRetorno = verificarTipo(tabela, ctx.fator_logico(0));

        // Para expressões lógicas, basta verificar se os tipos analisados são diferentes.
        for (AlgumaParser.Fator_logicoContext fatorLogico : ctx.fator_logico()) {
            TipoAlguma tipoAtual = verificarTipo(tabela, fatorLogico);
            if (tipoRetorno != tipoAtual && tipoAtual != TipoAlguma.INVALIDO)
                tipoRetorno = TipoAlguma.INVALIDO;
        }
        return tipoRetorno;
    }

    // Verifica o tipo de um fator lógico.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.Fator_logicoContext ctx) {
        TipoAlguma tipoRetorno = verificarTipo(tabela, ctx.parcela_logica());
        return tipoRetorno;
    }

    // Verifica o tipo de uma parcela lógica.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.Parcela_logicaContext ctx) {
        TipoAlguma tipoRetorno;

        if (ctx.exp_relacional() != null)
            tipoRetorno = verificarTipo(tabela, ctx.exp_relacional());
         else
            tipoRetorno = TipoAlguma.LOGICO;

        return tipoRetorno;
    }

    // Verifica o tipo de uma expressão relacional.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, AlgumaParser.Exp_relacionalContext ctx) {
        TipoAlguma tipoRetorno = verificarTipo(tabela, ctx.exp_aritmetica().get(0));

        if (ctx.exp_aritmetica().size() > 1) {
            TipoAlguma tipoAtual = verificarTipo(tabela, ctx.exp_aritmetica().get(1));

            // Verificação para saber se a expressão atual pode ser tratada como uma operação lógica.
            if (tipoRetorno == tipoAtual || verificaCompatibilidadeLogica(tipoRetorno, tipoAtual))
                tipoRetorno = TipoAlguma.LOGICO;
            else
                tipoRetorno = TipoAlguma.INVALIDO;
        }

        return tipoRetorno;

    }

    // Verificação padrão de tipos de variáveis a partir da tabela.
    public static TipoAlguma verificarTipo(TabelaDeSimbolos tabela, String nomeVar) {
        return tabela.verificar(nomeVar);
    }
}