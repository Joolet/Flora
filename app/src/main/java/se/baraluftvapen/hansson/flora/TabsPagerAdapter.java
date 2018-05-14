//Tabs 
package se.baraluftvapen.hansson.flora;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        return PageFragment.newInstance(position + 1);
    }

    //Antal
    @Override
    public int getCount() {
        return 3;
    }

    //namn
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0)
            return "Info";
        else if (position == 1)
            return "Källor";
        else if (position == 3)
            return "Rösta";
        else
            return "Kontakt";
    }
}