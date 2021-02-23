package com.habitrpg.android.habitica.interactors

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogLevelup10Binding
import com.habitrpg.android.habitica.events.ShareEvent
import com.habitrpg.android.habitica.executors.PostExecutionThread
import com.habitrpg.android.habitica.executors.ThreadExecutor
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.rxjava3.core.Flowable
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class LevelUpUseCase @Inject
constructor(private val soundManager: SoundManager, threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread,
            private val checkClassSelectionUseCase: CheckClassSelectionUseCase) : UseCase<LevelUpUseCase.RequestValues, Stats>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(requestValues: RequestValues): Flowable<Stats> {
        return Flowable.defer {
            soundManager.loadAndPlayAudio(SoundManager.SoundLevelUp)

            val suppressedModals = requestValues.user.preferences?.suppressModals
            if (suppressedModals?.levelUp == true) {
                showClassSelection(requestValues)
                return@defer Flowable.just<Stats>(requestValues.user.stats)
            }

            if (requestValues.newLevel == 10) {
                val binding = DialogLevelup10Binding.inflate(requestValues.activity.layoutInflater)
                binding.healerIconView.setImageBitmap(HabiticaIconsHelper.imageOfHealerLightBg())
                binding.mageIconView.setImageBitmap(HabiticaIconsHelper.imageOfMageLightBg())
                binding.rogueIconView.setImageBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
                binding.warriorIconView.setImageBitmap(HabiticaIconsHelper.imageOfWarriorLightBg())

                val alert = HabiticaAlertDialog(requestValues.activity)
                alert.setTitle(requestValues.activity.getString(R.string.levelup_header, requestValues.newLevel))
                alert.setAdditionalContentView(binding.root)
                alert.addButton(R.string.select_class, true) { _, _ ->
                    showClassSelection(requestValues)
                }
                alert.addButton(R.string.not_now, false)
                alert.isCelebratory = true

                if (!requestValues.activity.isFinishing) {
                    alert.enqueue()
                }
            } else {
                val customView = requestValues.activity.layoutInflater.inflate(R.layout.dialog_levelup, null)
                if (customView != null) {
                    val dialogAvatarView = customView.findViewById<AvatarView>(R.id.avatarView)
                    dialogAvatarView.setAvatar(requestValues.user)
                }

                val event = ShareEvent()
                event.sharedMessage = requestValues.activity.getString(R.string.share_levelup, requestValues.newLevel)
                val avatarView = AvatarView(requestValues.activity, showBackground = true, showMount = true, showPet = true)
                avatarView.setAvatar(requestValues.user)
                avatarView.onAvatarImageReady { t -> event.shareImage = t }

                val alert = HabiticaAlertDialog(requestValues.activity)
                alert.setTitle(requestValues.activity.getString(R.string.levelup_header, requestValues.newLevel))
                alert.setAdditionalContentView(customView)
                alert.addButton(R.string.onwards, true) { _, _ ->
                    showClassSelection(requestValues)
                }
                alert.addButton(R.string.share, false) { _, _ ->
                    EventBus.getDefault().post(event)
                }
                alert.isCelebratory = true

                if (!requestValues.activity.isFinishing) {
                    alert.enqueue()
                }
            }

            Flowable.just(requestValues.user.stats!!)
        }
    }

    private fun showClassSelection(requestValues: RequestValues) {
        checkClassSelectionUseCase.observable(CheckClassSelectionUseCase.RequestValues(requestValues.user, true, null, requestValues.activity))
                .subscribe({ }, RxErrorHandler.handleEmptyError())
    }

    class RequestValues(val user: User, val level: Int?, val activity: AppCompatActivity) : UseCase.RequestValues {
        val newLevel: Int = level ?: 0
    }
}
