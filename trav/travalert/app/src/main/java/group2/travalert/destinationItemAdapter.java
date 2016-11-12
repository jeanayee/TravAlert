package group2.travalert;

/**
 * Created by Gigi on 4/17/16.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class destinationItemAdapter extends ArrayAdapter<String> {

    int resource;

    public destinationItemAdapter(Context ctx, int res, List<String> items) {
        super(ctx, res, items);
        resource = res;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout destinationView;
        String destination = getItem(position);

        if (convertView == null) {
            destinationView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
            vi.inflate(resource, destinationView, true);
        } else {
            destinationView = (LinearLayout) convertView;
        }

        TextView destinationAddress = (TextView) destinationView.findViewById(R.id.destination_text);
        destinationAddress.setText(destination);

        return destinationView;
    }
}
