package group2.travalert;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {
    private Activity activity;
    private List<Contact> contacts;
    private int row;
    private Contact objBean;
    boolean[] isChecked;

    public ContactAdapter(Activity act, int row, List<Contact> items, boolean[] checked) {
        super(act, row, items);

        this.activity = act;
        this.row = row;
        this.contacts = items;
        this.isChecked = checked;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(row, null);

            holder = new ViewHolder();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if ((contacts == null) || ((position + 1) > contacts.size()))
            return view;

        objBean = contacts.get(position);

        holder.name = (TextView) view.findViewById(R.id.contactname);
        holder.PhoneNo = (TextView) view.findViewById(R.id.phonenumber);
        holder.checkBox = (CheckBox) view.findViewById(R.id.checkBox);

        if (holder.name != null && null != objBean.getName()
                && objBean.getName().trim().length() > 0) {
            holder.name.setText(Html.fromHtml(objBean.getName()));
        }
        if (holder.PhoneNo != null && null != objBean.getNumber()
                && objBean.getNumber().trim().length() > 0) {
            holder.PhoneNo.setText(Html.fromHtml(objBean.getNumber()));
        }

        if (isChecked[position] == true)
            holder.checkBox.setChecked(true);
        else
            holder.checkBox.setChecked(false);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean ischecked) {
                if (buttonView.isChecked())
                    isChecked[position] = true;
                else
                    isChecked[position] = false;
            }
        });

        return view;
    }

    public class ViewHolder {
        public TextView name, PhoneNo;
        CheckBox checkBox;
    }
}