package com.handysparksoft.trackmap.features.entries.sort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.handysparksoft.trackmap.core.platform.viewbinding.FragmentViewBindingHolder
import com.handysparksoft.trackmap.databinding.SortEntriesBottomSheetLayoutBinding

class SortEntriesBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private val bindingHolder = FragmentViewBindingHolder<SortEntriesBottomSheetLayoutBinding>()
    private val binding get() = bindingHolder.binding

    private var onSortByDateClickListener: (() -> Unit)? = null
    private var onSortByNameClickListener: (() -> Unit)? = null
    private var onSortByParticipantsClickListener: (() -> Unit)? = null
    private var onSortByOwnedClickListener: (() -> Unit)? = null
    private var onSortByLiveTrackingClickListener: (() -> Unit)? = null

    fun onSortByDateClick(listener: () -> Unit) {
        onSortByDateClickListener = listener
    }

    fun onSortByNameClick(listener: () -> Unit) {
        onSortByNameClickListener = listener
    }

    fun onSortByParticipantsClick(listener: () -> Unit) {
        onSortByParticipantsClickListener = listener
    }

    fun onSortByOwnedClick(listener: () -> Unit) {
        onSortByOwnedClickListener = listener
    }

    fun onSortByLiveTrackingClick(listener: () -> Unit) {
        onSortByLiveTrackingClickListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingHolder.createBinding(this) {
            SortEntriesBottomSheetLayoutBinding.inflate(layoutInflater, container, false)
        }
        return bindingHolder.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.sortByDateTextView.setOnClickListener {
            onSortByDateClickListener?.invoke()
            dismiss()
        }
        binding.sortByName.setOnClickListener {
            onSortByNameClickListener?.invoke()
            dismiss()

        }
        binding.sortByParticipantCount.setOnClickListener {
            onSortByParticipantsClickListener?.invoke()
            dismiss()
        }
        binding.sortByOwned.setOnClickListener {
            onSortByOwnedClickListener?.invoke()
            dismiss()
        }
        binding.sortByLiveTracking.setOnClickListener {
            onSortByLiveTrackingClickListener?.invoke()
            dismiss()
        }
    }
}
