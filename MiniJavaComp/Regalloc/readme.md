<h1>Análise de Longevidade e Seleção de Registradores</h1>

<b>Para gerar os arquivos-fonte do MiniJavaParser:</b>
<ol>
    <p>No diretório core, execute:</p>
    <ul>
        <li>javacc grammar.jj</li>
</ol>

<b>Para compilar o MiniJavaParser e as dependências:</b>
<ol>
    <p>No diretório core, execute:</p>
    <ul>
        <li>javac -d classes ../intermediate_code/Assem/*.java ../intermediate_code/Canon/*.java ../intermediate_code/FragAux/*.java ../intermediate_code/Frame/*.java ../intermediate_code/Mips/*.java ../intermediate_code/Symbol/*.java ../intermediate_code/Temp/*.java ../intermediate_code/Tree/*.java ../intermediate_code/Util/*.java ../intermediate_code/Visitor/*.java ../semantic_analysis/ast/*.java ../semantic_analysis/symboltable/*.java ../semantic_analysis/visitor/*.java ../graph/*.java ../liveness_analysis/FlowGraph/*.java ../regalloc/*.java MiniJavaParser.java</li>
</ol>

<b>Para executar:</b>
<ol>
    <p>No diretório core/classes, execute:</p>
    <ul>
        <li>java MiniJavaParser ../../programs/NomeProgramaDeTeste.java</li>
</ol>