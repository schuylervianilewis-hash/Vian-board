package com.example.vault.ui

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.foundation.utils.ResourceUtils
import com.example.vault.model.VaultDatabase
import com.example.vault.model.VaultEntry
import com.example.vault.storage.VaultRepository
import com.example.vault.totp.TotpGenerator

/**
 * In-Keyboard Vault Overlay View.
 * Provides zero-clipboard secret injection for usernames, passwords, and TOTP 2FA codes.
 */
class VaultOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), VaultRepository.VaultStateListener {

    interface VaultOverlayActionListener {
        fun onInjectText(text: String)
        fun onDismissRequested()
        fun onSpaceClicked()
        fun onDeleteClicked()
        fun onEnterClicked()
    }

    var actionListener: VaultOverlayActionListener? = null
    val repository = VaultRepository(context)

    private val containerLayout: LinearLayout
    private val headerLayout: LinearLayout
    private val titleTextView: TextView
    private val lockButton: Button
    private val contentFrame: FrameLayout

    // Unlocked UI elements
    private var unlockedLayout: LinearLayout? = null
    private var searchEditText: EditText? = null
    private var entriesContainer: LinearLayout? = null

    // Locked UI elements
    private var lockedLayout: LinearLayout? = null
    private var passwordEditText: EditText? = null

    init {
        repository.listener = this
        setBackgroundColor(Color.parseColor("#11111B"))

        containerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // 1. Header
        headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                ResourceUtils.dpToPx(context, 12f).toInt(),
                ResourceUtils.dpToPx(context, 8f).toInt(),
                ResourceUtils.dpToPx(context, 12f).toInt(),
                ResourceUtils.dpToPx(context, 8f).toInt()
            )
            setBackgroundColor(Color.parseColor("#1E1E2E"))
        }

        titleTextView = TextView(context).apply {
            text = "🔒 Security Vault"
            setTextColor(Color.parseColor("#CDD6F4"))
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        headerLayout.addView(titleTextView)

        lockButton = Button(context).apply {
            text = "Lock"
            textSize = 12f
            setTextColor(Color.parseColor("#F38BA8"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                repository.lockVault()
            }
        }
        headerLayout.addView(lockButton)
        containerLayout.addView(headerLayout)

        // 2. Middle Content Area
        contentFrame = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        containerLayout.addView(contentFrame)

        // 3. Unified 4-Button Bottom Bar
        val bottomBar = createUnifiedBottomBar()
        containerLayout.addView(bottomBar)

        addView(containerLayout)

        updateUiState()
    }

    private fun createUnifiedBottomBar(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#181825"))
            setPadding(
                ResourceUtils.dpToPx(context, 8f).toInt(),
                ResourceUtils.dpToPx(context, 4f).toInt(),
                ResourceUtils.dpToPx(context, 8f).toInt(),
                ResourceUtils.dpToPx(context, 4f).toInt()
            )

            val btnAbc = createBarButton("ABC", "#89B4FA") {
                actionListener?.onDismissRequested()
            }
            val btnSpace = createBarButton("SPACE", "#BAC2DE") {
                actionListener?.onSpaceClicked()
            }
            val btnDel = createBarButton("⌫", "#F38BA8") {
                actionListener?.onDeleteClicked()
            }
            val btnEnter = createBarButton("↵", "#A6E3A1") {
                actionListener?.onEnterClicked()
            }

            addView(btnAbc, LinearLayout.LayoutParams(0, ResourceUtils.dpToPx(context, 42f).toInt(), 1f).apply { setMargins(4, 0, 4, 0) })
            addView(btnSpace, LinearLayout.LayoutParams(0, ResourceUtils.dpToPx(context, 42f).toInt(), 2.5f).apply { setMargins(4, 0, 4, 0) })
            addView(btnDel, LinearLayout.LayoutParams(0, ResourceUtils.dpToPx(context, 42f).toInt(), 1f).apply { setMargins(4, 0, 4, 0) })
            addView(btnEnter, LinearLayout.LayoutParams(0, ResourceUtils.dpToPx(context, 42f).toInt(), 1f).apply { setMargins(4, 0, 4, 0) })
        }
    }

    private fun createBarButton(label: String, colorHex: String, onClick: () -> Unit): Button {
        return Button(context).apply {
            text = label
            setTextColor(Color.parseColor(colorHex))
            textSize = 13f
            setBackgroundColor(Color.parseColor("#313244"))
            setOnClickListener { onClick() }
        }
    }

    fun updateUiState() {
        contentFrame.removeAllViews()

        if (!repository.isVaultCreated()) {
            titleTextView.text = "🔒 Vault (Uninitialized)"
            lockButton.visibility = View.GONE
            contentFrame.addView(createUninitializedView())
        } else if (!repository.isUnlocked()) {
            titleTextView.text = "🔒 Vault Locked"
            lockButton.visibility = View.GONE
            contentFrame.addView(createLockedView())
        } else {
            titleTextView.text = "🔓 Vault Unlocked"
            lockButton.visibility = View.VISIBLE
            contentFrame.addView(createUnlockedView())
        }
    }

    private fun createUninitializedView(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 16, 32, 16)

            val tv = TextView(context).apply {
                text = "Vault is not set up yet.\nOpen Vian Board Settings > Vault to configure your master password."
                setTextColor(Color.parseColor("#BAC2DE"))
                textSize = 13f
                gravity = Gravity.CENTER
            }
            addView(tv)
        }
    }

    private fun createLockedView(): View {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 16, 32, 16)
        }

        val passInput = EditText(context).apply {
            hint = "Enter Master Password"
            setHintTextColor(Color.parseColor("#6C7086"))
            setTextColor(Color.parseColor("#CDD6F4"))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setBackgroundColor(Color.parseColor("#313244"))
            setPadding(24, 20, 24, 20)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        passwordEditText = passInput
        layout.addView(passInput)

        val unlockBtn = Button(context).apply {
            text = "Unlock Vault"
            setBackgroundColor(Color.parseColor("#89B4FA"))
            setTextColor(Color.parseColor("#11111B"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 16
            }
            setOnClickListener {
                val passChars = passInput.text.toString().toCharArray()
                val success = repository.unlockVault(passChars)
                if (!success) {
                    Toast.makeText(context, "Invalid master password", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(unlockBtn)
        lockedLayout = layout
        return layout
    }

    private fun createUnlockedView(): View {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // Search box
        val searchBox = EditText(context).apply {
            hint = "🔍 Search credentials..."
            setHintTextColor(Color.parseColor("#6C7086"))
            setTextColor(Color.parseColor("#CDD6F4"))
            textSize = 13f
            setBackgroundColor(Color.parseColor("#1E1E2E"))
            setPadding(24, 16, 24, 16)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    filterEntries(s?.toString() ?: "")
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        searchEditText = searchBox
        layout.addView(searchBox)

        // Scrollable entries
        val scrollView = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
        }
        val entriesList = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 8)
        }
        entriesContainer = entriesList
        scrollView.addView(entriesList)
        layout.addView(scrollView)

        unlockedLayout = layout
        filterEntries("")
        return layout
    }

    private fun filterEntries(query: String) {
        val container = entriesContainer ?: return
        container.removeAllViews()

        val db = repository.getUnlockedDatabase() ?: return
        val filtered = if (query.isEmpty()) {
            db.entries
        } else {
            db.entries.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.username.contains(query, ignoreCase = true) ||
                it.url.contains(query, ignoreCase = true)
            }
        }

        if (filtered.isEmpty()) {
            val emptyTv = TextView(context).apply {
                text = if (query.isEmpty()) "Vault is empty. Add entries in Settings." else "No matching credentials."
                setTextColor(Color.parseColor("#6C7086"))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            container.addView(emptyTv)
            return
        }

        for (entry in filtered) {
            container.addView(createEntryCard(entry))
        }
    }

    private fun createEntryCard(entry: VaultEntry): View {
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1E1E2E"))
            setPadding(16, 12, 16, 12)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 8
            }
        }

        // Title and username row
        val titleRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleTv = TextView(context).apply {
            text = entry.title
            setTextColor(Color.parseColor("#CDD6F4"))
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val userTv = TextView(context).apply {
            text = entry.username
            setTextColor(Color.parseColor("#A6ADC8"))
            textSize = 12f
        }
        titleRow.addView(titleTv)
        titleRow.addView(userTv)
        card.addView(titleRow)

        // Quick injection pills
        val pillsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 8
            }
        }

        if (entry.username.isNotEmpty()) {
            val userPill = createPillButton("User", "#89B4FA") {
                actionListener?.onInjectText(entry.username)
            }
            pillsRow.addView(userPill)
        }

        if (entry.password.isNotEmpty()) {
            val passPill = createPillButton("Pass", "#F38BA8") {
                actionListener?.onInjectText(entry.password)
            }
            pillsRow.addView(passPill)
        }

        if (entry.totpSecret.isNotEmpty()) {
            val totpCode = TotpGenerator.generateCurrentCode(entry.totpSecret)
            val remainingSec = TotpGenerator.getRemainingSeconds()
            val label = if (totpCode != null) "2FA: $totpCode (${remainingSec}s)" else "2FA"
            val totpPill = createPillButton(label, "#A6E3A1") {
                val code = TotpGenerator.generateCurrentCode(entry.totpSecret)
                if (code != null) {
                    actionListener?.onInjectText(code)
                }
            }
            pillsRow.addView(totpPill)
        }

        card.addView(pillsRow)
        return card
    }

    private fun createPillButton(label: String, colorHex: String, onClick: () -> Unit): Button {
        return Button(context).apply {
            text = label
            setTextColor(Color.parseColor(colorHex))
            textSize = 11f
            setBackgroundColor(Color.parseColor("#313244"))
            setPadding(16, 4, 16, 4)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ResourceUtils.dpToPx(context, 32f).toInt()).apply {
                rightMargin = 8
            }
            setOnClickListener { onClick() }
        }
    }

    // VaultRepository.VaultStateListener
    override fun onVaultUnlocked(database: VaultDatabase) {
        updateUiState()
    }

    override fun onVaultLocked() {
        updateUiState()
    }
}
