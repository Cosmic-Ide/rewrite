package org.cosmicide.rewrite.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dalvik.system.DexClassLoader
import dalvik.system.DexFile
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.cosmicide.project.Project
import org.cosmicide.rewrite.compile.Compiler
import org.cosmicide.rewrite.databinding.FragmentCompileInfoBinding
import org.cosmicide.rewrite.editor.util.EditorUtil
import org.cosmicide.rewrite.util.ProjectHandler
import java.io.OutputStream
import java.io.PrintStream
import java.lang.reflect.Modifier

class ProjectOutputFragment : Fragment() {
    private lateinit var project: Project
    private lateinit var binding: FragmentCompileInfoBinding
    private lateinit var compiler: Compiler

    override fun onCreate(savedInstanceState: Bundle?) {
        println("ProjectOutputFragment.onCreate")
        project = ProjectHandler.project!!
        compiler = Compiler(project)
        super.onCreate(savedInstanceState)
    }

    @Suppress("Deprecation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompileInfoBinding.inflate(inflater, container, false)

        binding.infoEditor.colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
        binding.infoEditor.setEditorLanguage(TextMateLanguage.create("source.build", false))
        binding.infoEditor.editable = false
        binding.infoEditor.isWordwrap = true
        binding.infoEditor.setTextSize(14f)
        EditorUtil.setEditorFont(binding.infoEditor)
        // Inflate the layout for this fragment
        CoroutineScope(lifecycleScope.coroutineContext).launch {
            val systemOut = PrintStream(object : OutputStream() {
                override fun write(p0: Int) {
                    val text = binding.infoEditor.text
                    val cursor = text.cursor
                    text.insert(cursor.rightLine, cursor.rightColumn, p0.toChar().toString())
                }
            })
            System.setOut(systemOut)
            System.setErr(systemOut)
            val dexFile = DexFile(project.binDir.resolve("classes.dex"))
            val classes = dexFile.entries().toList()
            val className = classes[0]
            val loader = DexClassLoader(
                project.binDir.resolve("classes.dex").absolutePath,
                project.binDir.toString(),
                null,
                this.javaClass.classLoader
            )
            val clazz = loader.loadClass(className)
            if (clazz.declaredMethods.any { it.name == "main" }) {
                val method = clazz.getDeclaredMethod("main", Array<String>::class.java)
                if (Modifier.isStatic(method.modifiers)) {
                    method.invoke(null, arrayOf<String>())
                } else if (Modifier.isPublic(method.modifiers)) {
                    method.invoke(clazz.newInstance(), arrayOf<String>())
                } else {
                    System.err.println("Main method is not public or static")
                }
            } else {
                System.err.println("No main method found")
            }
        }
        return binding.root
    }
}