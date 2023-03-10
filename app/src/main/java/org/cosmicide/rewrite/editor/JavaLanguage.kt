package org.cosmicide.rewrite.editor

import android.os.Bundle
import android.util.Log
import androidx.annotation.WorkerThread
import com.tyron.javacompletion.JavaCompletions
import com.tyron.javacompletion.options.JavaCompletionOptionsImpl
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.widget.CodeEditor
import org.cosmicide.project.Project
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Level

class JavaLanguage(
    private val editor: CodeEditor,
    private val project: Project,
    private val file: File
) : TextMateLanguage(
    grammarRegistry.findGrammar("source.java"),
    grammarRegistry.findLanguageConfiguration("source.java"),
    grammarRegistry,
    themeRegistry,
    false
) {

    private val completions: JavaCompletions by lazy { JavaCompletions() }
    private val path: Path = file.toPath()

    init {
        val options = JavaCompletionOptionsImpl(
            "${project.binDir.absolutePath}${File.pathSeparator}autocomplete.log",
            Level.ALL,
            emptyList(),
            emptyList()
        )
        completions.initialize(URI("file://${project.root.absolutePath}"), options)
    }

    @WorkerThread
    override fun requireAutoComplete(
        content: ContentReference,
        position: CharPosition,
        publisher: CompletionPublisher,
        extraArguments: Bundle
    ) {
        try {
            val text = editor.text.toString()
            Files.write(path, text.toByteArray())
            val result = completions.project.getCompletionResult(path, position.line, position.column)
            result.completionCandidates.forEach { candidate ->
                if (candidate.name != "<error>") {
                    val item = SimpleCompletionItem(
                        candidate.name,
                        candidate.detail.orElse("Unknown"),
                        result.prefix.length,
                        candidate.name
                    )
                    publisher.addItem(item)
                }
            }
        } catch (e: Throwable) {
            if (e !is InterruptedException) {
                Log.e(TAG, "Failed to fetch code suggestions", e)
            }
        }
        super.requireAutoComplete(content, position, publisher, extraArguments)
    }

    companion object {
        private const val TAG = "JavaLanguage"
        private val grammarRegistry = GrammarRegistry.getInstance()
        private val themeRegistry = ThemeRegistry.getInstance()
    }
}