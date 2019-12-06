package com.dawning.upgrade

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.layout_dialog_upgrade.view.*

class UpgradeDialogBuilder(context: Context,
                           config: UpgradeConfig?,
                           private var upgradeBean: UpgradeBean) : AlertDialog.Builder(context) {

    private var mView: View ?= null
    private var mPositiveClickListener: View.OnClickListener ?= null
    init {
        if (upgradeBean.mustUpgrade) {
            mView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_upgrade, null)
            setView(mView)
        }
        else {
            setNegativeButton(config?.cancel, null)
        }

        setTitle(config?.title)
        setMessage(config?.content)

        setPositiveButton(config?.affirm, null)
    }

    fun setProgress(progress: Int) {
        mView?.pb_progress?.setProgress(progress)
    }

    fun setOnPositiveClickListener(positiveClickListener: View.OnClickListener) {
        mPositiveClickListener = positiveClickListener
    }

    override fun show(): AlertDialog {
        val dialog = super.show()
        val button = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        if (button != null) {
            button?.setOnClickListener {
                if (mPositiveClickListener != null) mPositiveClickListener?.onClick(null)
                if (!upgradeBean.mustUpgrade) dialog.dismiss()}
        }
        return dialog
    }
}