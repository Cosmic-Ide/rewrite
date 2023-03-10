package org.cosmicide.rewrite.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import org.cosmicide.project.Java
import org.cosmicide.project.Kotlin
import org.cosmicide.project.Project
import org.cosmicide.rewrite.databinding.FragmentEditorBinding
import org.cosmicide.rewrite.editor.JavaLanguage
import org.cosmicide.rewrite.editor.KotlinLanguage
import org.cosmicide.rewrite.model.FileViewModel
import org.cosmicide.rewrite.util.Constants
import org.cosmicide.rewrite.util.FileIndex
import java.io.File

class EditorFragment : Fragment() {

    private var project: Project? = null
    private lateinit var fileIndex: FileIndex
    private lateinit var binding: FragmentEditorBinding
    private lateinit var fileViewModel: FileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditorBinding.inflate(inflater, container, false)

        arguments?.getString(Constants.PROJECT_DIR)?.let { dir ->
            project = File(dir).let {
                when {
                    it.isDirectory && it.resolve("src/main/java").exists() -> Project(it, Java)
                    it.isDirectory && it.resolve("src/main/kotlin").exists() -> Project(it, Kotlin)
                    else -> null
                }
            }
            fileIndex = FileIndex(project!!)
        }

        fileViewModel = ViewModelProvider(this).get(FileViewModel::class.java)

        val files = fileIndex.getFiles()
        if (files.isNotEmpty()) {
            fileViewModel.updateFiles(files.toMutableList())
            for (file in files) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(file.name))
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                fileViewModel.setCurrentPosition(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                fileViewModel.removeFile(fileViewModel.files.value!![tab.position])
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                binding.editor.setText(fileViewModel.files.value!![tab.position].readText())
            }
        })

        fileViewModel.files.observe(viewLifecycleOwner) { files ->
            binding.tabLayout.removeAllTabs()
            files.forEach { file ->
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(file.name))
            }
        }

        fileViewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            if (position == -1) {
                return@observe
            }
            binding.editor.setText(fileViewModel.currentFile?.readText())
            setEditorLanguage()
        }

        fileViewModel.addFile(File(project!!.srcDir.invoke(), "Main." + project!!.language.extension))

        binding.editor.setTextSize(20f)

        return binding.root
    }

    private fun setEditorLanguage() {
        val file = fileViewModel.currentFile
        binding.editor.setEditorLanguage(
            when (file?.extension) {
                "kt" -> KotlinLanguage(binding.editor, project!!, file)
                "java" -> JavaLanguage(binding.editor, project!!, file)
                else -> EmptyLanguage()
            }
        )
        binding.editor.colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
    }

    override fun onDestroy() {
        super.onDestroy()
        fileViewModel.currentPosition.value?.let { pos ->
            fileViewModel.currentFile?.takeIf { it.exists() }?.writeText(binding.editor.text.toString())
            fileIndex.putFiles(pos, fileViewModel.files.value!!)
        }
    }
}