package compiladores.alguma.semantico;

import java.util.HashMap;
import java.util.Map;
import static compiladores.alguma.semantico.AlgumaSemanticoUtils.reduzNome;

public class TabelaDeSimbolos {

    private final Map<String, EntradaTabelaDeSimbolos> tabela;

    public TabelaDeSimbolos() {
        this.tabela = new HashMap<>();
    }

    // Tipos possíveis de variáveis.
    public enum TipoAlguma {
        INTEIRO,
        REAL,
        LITERAL,
        LOGICO,
        VOID,
        REGISTRO,
        INVALIDO
    }

    // Tipos possíveis de declarações.
    public enum TipoEntrada {
        VARIAVEL, // Para fins de simplificação, tudo que não for um procedimento ou função será considerado uma variável.
        PROCEDIMENTO,
        FUNCAO
    }

    class EntradaTabelaDeSimbolos {
        String nome;
        TipoAlguma tipo;
        TipoEntrada tipoE;

        private EntradaTabelaDeSimbolos(String nome, TipoAlguma tipo, TipoEntrada tipoE) {
            this.nome = nome;
            this.tipo = tipo;
            this.tipoE = tipoE;
        }
    }

    public TipoAlguma verificar(String nome) {
        // Remoção do índice de um vetor.
        nome = reduzNome(nome, "[");

        return tabela.get(nome).tipo;
    }

    public void adicionar(String nome, TipoAlguma tipo, TipoEntrada tipoE) {
        // Remoção do índice de um vetor.
        nome = reduzNome(nome, "[");

        tabela.put(nome, new EntradaTabelaDeSimbolos(nome, tipo, tipoE));
    }    

    public boolean existe(String nome) {
        // Remoção do índice de um vetor.
        nome = reduzNome(nome, "[");

        return tabela.containsKey(nome);
    }
}