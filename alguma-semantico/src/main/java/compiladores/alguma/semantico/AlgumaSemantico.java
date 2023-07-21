package compiladores.alguma.semantico;

// Importações básicas para o funcionamento do programa.
import static compiladores.alguma.semantico.AlgumaSemanticoUtils.verificarTipo;
import static compiladores.alguma.semantico.AlgumaSemanticoUtils.adicionaErroSemantico;
import static compiladores.alguma.semantico.AlgumaSemanticoUtils.verificaCompatibilidade;
import compiladores.alguma.semantico.TabelaDeSimbolos.TipoAlguma;
import org.antlr.v4.runtime.Token;

// A classe implementa os métodos "visit", que define a estrutura dos códigos analisados.
public class AlgumaSemantico extends AlgumaBaseVisitor<Void> {

    // Tabela para armazenar os escopos ao longo da análise
    TabelaDeSimbolos tabela;

    // Criação dos escopos
    static Escopos escoposAninhados = new Escopos();
    
    TabelaDeSimbolos tabelaEscopo;

    // Método que adiciona a variável que está sendo analisado à tabela.
    public void adicionaVariavelTabela(String nome, String tipo, Token nomeT, Token tipoT) {
        tabelaEscopo = escoposAninhados.obterEscopoAtual();

        TipoAlguma tipoItem;

        switch (tipo) {
            case "literal":
                tipoItem = TipoAlguma.LITERAL;
                break;
            case "inteiro":
                tipoItem = TipoAlguma.INTEIRO;
                break;
            case "real":
                tipoItem = TipoAlguma.REAL;
                break;
            case "logico":
                tipoItem = TipoAlguma.LOGICO;
                break;
            default:
                tipoItem = TipoAlguma.INVALIDO;
                break;
        }

        // Caso o tipo seja inválido, exibe a mensagem de que o tipo não foi declarado.
        if (tipoItem == TipoAlguma.INVALIDO)
            adicionaErroSemantico(tipoT, "tipo " + tipo + " nao declarado");

        // Verifica se a variável já foi declarada, ou seja, já foi adicionada na tabela.
        if (!tabelaEscopo.existe(nome))
            tabelaEscopo.adicionar(nome, tipoItem);
        else
            adicionaErroSemantico(nomeT, "identificador " + nome + " ja declarado anteriormente");
    }

    @Override
    public Void visitPrograma(AlgumaParser.ProgramaContext ctx) {
        // Inicialização do programa.
        tabela = new TabelaDeSimbolos();
        return super.visitPrograma(ctx);
    }

    @Override
    public Void visitDeclaracoes(AlgumaParser.DeclaracoesContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();
        
        // Verifica a declaração atual.
        for (AlgumaParser.Declaracao_globalContext declaracao : ctx.declaracao_global())
            visitDeclaracao_global(declaracao);
        
        return super.visitDeclaracoes(ctx);
    }

    @Override
    public Void visitDeclaracao_local(AlgumaParser.Declaracao_localContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();

        String tipoVariavel;
        String nomeVariavel;

        // Tenta identificar uma declaração.
        if (ctx.getText().contains("declare")) {
            tipoVariavel = ctx.variavel().tipo().getText();

            // Adiciona a variável atual na tabela.
            for (AlgumaParser.IdentificadorContext ident : ctx.variavel().identificador()) {
                nomeVariavel = ident.getText();
                adicionaVariavelTabela(nomeVariavel, tipoVariavel, ident.getStart(), ctx.variavel().tipo().getStart());
            }
        }

        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Void visitCmdLeia(AlgumaParser.CmdLeiaContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();

        for (AlgumaParser.IdentificadorContext id : ctx.identificador())
            // Verifica se a variável já foi declarada.
            if (!tabela.existe(id.getText()))
                adicionaErroSemantico(id.getStart(), "identificador " + id.getText() + " nao declarado");

        return super.visitCmdLeia(ctx);
    }

    @Override
    public Void visitCmdEscreva(AlgumaParser.CmdEscrevaContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();
        
        TipoAlguma tipo;

        for (AlgumaParser.ExpressaoContext expressao : ctx.expressao())
            tipo = verificarTipo(tabela, expressao);

        return super.visitCmdEscreva(ctx);
    }

    @Override
    public Void visitCmdEnquanto(AlgumaParser.CmdEnquantoContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();
        
        TipoAlguma tipo = verificarTipo(tabela, ctx.expressao());
        
        return super.visitCmdEnquanto(ctx);
    }

    @Override
    public Void visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();
        
        TipoAlguma tipoExpressao = verificarTipo(tabela, ctx.expressao());
        
        String varNome = ctx.identificador().getText();
        
        if (tipoExpressao != TipoAlguma.INVALIDO) {
            // Caso a variável não tenha sido declarada, informa o erro.
            if (!tabela.existe(varNome)) {
                adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
            } else {
                // Caso contrário, identifica o tipo da variável para as condições posteriores.
                TipoAlguma varTipo = verificarTipo(tabela, varNome);
                
                //  Verifica-se se o valor a ser trabalhado é inteiro ou real.
                if (varTipo == TipoAlguma.INTEIRO || varTipo == TipoAlguma.REAL) {
                    if (!verificaCompatibilidade(varTipo, tipoExpressao)) {
                        // A incompatibilidade ocorre ao comparar um número com um literal 
                        // quando a parte restante da parcela não é inteira.
                        if (tipoExpressao != TipoAlguma.INTEIRO) {
                            adicionaErroSemantico(ctx.identificador().getStart(), "atribuicao nao compativel para " + ctx.identificador().getText());
                        }
                    }
                // Verifica-se a diferença de tipos quando não há números que necessitem 
                // de tratamento especial.
                } else if (varTipo != tipoExpressao)
                    adicionaErroSemantico(ctx.identificador().getStart(), "atribuicao nao compativel para " + ctx.identificador().getText());
            }
        }
        
        return super.visitCmdAtribuicao(ctx);
    }

}