import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.cookease.app.R
import com.cookease.app.ui.addrecipe.RecipeStep1Fragment

class AddFragment : Fragment(R.layout.fragment_add) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Only load Step 1 if the container is empty (first time opening)
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecipeStep1Fragment())
                .commit()
        }
    }
}