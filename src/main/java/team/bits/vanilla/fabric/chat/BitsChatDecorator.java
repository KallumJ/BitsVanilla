package team.bits.vanilla.fabric.chat;

import net.minecraft.network.message.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class BitsChatDecorator implements MessageDecorator, Closeable {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final BlockingQueue<DecorationRequest> requestQueue = new LinkedBlockingQueue<>();

    public BitsChatDecorator() {
        // the actual decorator runs on a new thread to improve performance since
        // it may have to run many times per second
        this.executor.execute(this::run);
    }

    @Override
    public CompletableFuture<Text> decorate(@Nullable ServerPlayerEntity sender, @NotNull Text message) {
        // enqueue a decoration request for the message to the decorator thread
        CompletableFuture<Text> futureResult = new CompletableFuture<>();
        this.requestQueue.add(new DecorationRequest(message, futureResult));
        return futureResult;
    }

    public void run() {
        while (true) {
            try {
                DecorationRequest request = this.requestQueue.take();
                // we attempt to decorate the message. if we encounter an exception during
                // the decoration process, log the exception and send the undecorated message back
                try {
                    Text decoratedMessage = this.decorateMessage(request.message());
                    request.future().complete(decoratedMessage);
                } catch (Exception ex) {
                    LOGGER.error("Exception in chat decorator", ex);
                    request.future().complete(request.message());
                }
            } catch (InterruptedException e) {
                // if the thread is interrupted, the server is shutting down
                Thread.currentThread().interrupt();
                LOGGER.warn("Chat decorator shutting down");
                return;
            }
        }
    }

    @Override
    public void close() throws IOException {
        // shutdownNow will trigger an interrupt of the active thread
        this.executor.shutdownNow();
    }

    private @NotNull Text decorateMessage(@NotNull Text message) {
        // run the parser on the content of the message
        FormattedTextParser parser = new FormattedTextParser(message.getString());
        Stack<FormattedTextParser.FormattedBlock> blocks = parser.parse();

        // transform the parsed message blocks into a single formatted message
        MutableText decoratedMessage = Text.empty();
        for (FormattedTextParser.FormattedBlock block : blocks) {
            decoratedMessage.append(
                    Text.literal(block.text())
                            .setStyle(FormatUtils.formatTypeToStyle(block.type()))
            );
        }
        return decoratedMessage;
    }

    private record DecorationRequest(@NotNull Text message, @NotNull CompletableFuture<Text> future) {
    }
}
