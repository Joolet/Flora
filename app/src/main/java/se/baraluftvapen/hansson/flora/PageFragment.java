//Tabs
package se.baraluftvapen.hansson.flora;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PageFragment extends Fragment {
    private static final String ARG_PAGE_NUMBER = "page_number";

    public PageFragment() {
    }

    public static PageFragment newInstance(int page) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_NUMBER, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        int page = getArguments().getInt(ARG_PAGE_NUMBER, -1);

        if (page == 3) {
            rootView = inflater.inflate(R.layout.tab3, container, false);
        }
        else if (page == 2) {
            rootView = inflater.inflate(R.layout.tab2, container, false);
        }
        else if (page == 4) {
            rootView = inflater.inflate(R.layout.tab4, container, false);
        }
        else {
            rootView = inflater.inflate(R.layout.tab1, container, false);
        }
        return rootView;
    }
}
