package org.javacs;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompletionsBase {
    protected static final Logger LOG = Logger.getLogger("main");

    protected Set<String> insertText(String file, int row, int column) throws IOException {
        List<? extends CompletionItem> items = items(file, row, column);

        return items
                .stream()
                .map(CompletionItem::getInsertText)
                .collect(Collectors.toSet());
    }

    protected Set<String> documentation(String file, int row, int column) throws IOException {
        List<? extends CompletionItem> items = items(file, row, column);

        return items
                .stream()
                .flatMap(i -> {
                    if (i.getDocumentation() != null)
                        return Stream.of(i.getDocumentation().trim());
                    else
                        return Stream.empty();
                })
                .collect(Collectors.toSet());
    }

    protected static final JavaLanguageServer server = LanguageServerFixture.getJavaLanguageServer();

    protected List<? extends CompletionItem> items(String file, int row, int column) {
        URI uri = FindResource.uri(file);
        TextDocumentPositionParams position = new TextDocumentPositionParams(
                new TextDocumentIdentifier(uri.toString()),
                uri.toString(),
                new Position(row - 1, column - 1)
        );

        try {
            return server.getTextDocumentService().completion(position).get().getRight().getItems();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}