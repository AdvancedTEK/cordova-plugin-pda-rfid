package cordova.plugin.first.plugin.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cordova.plugin.first.plugin.DialogList;

import java.util.*;

public class DialogListAdapter extends RecyclerView.Adapter<DialogListAdapter.DialogListViewHolder> {

    private Context context;
    private List<String> epcStringList;

    public DialogListAdapter(Context context) {
        this.context = context;
        this.epcStringList = new ArrayList<>();
    }

    public void add(String epcString) {
        if (!epcStringList.contains(epcString)) {
            this.epcStringList.add(epcString);
            notifyDataSetChanged();
            DialogList.updateButtons(getItemCount());
        }
    }

    public String getEpcStringList() {
        if (getItemCount() == 1) {
            return epcStringList.get(0);
        } else {
            String tags = String.join(";", epcStringList);
            Log.d("test", "getEpcStringList: Only one tag can be selected! " + tags);
            return tags;
        }
    }

    public void clearEpcStringList() {
        epcStringList.clear();
        notifyDataSetChanged();
        DialogList.updateButtons(getItemCount());
    }

    @NonNull
    @Override
    public DialogListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        String packageName = context.getPackageName();
        Resources resources = context.getResources();
        int viewId = resources.getIdentifier("dialog_recycler_item", "layout", packageName);

        View view = LayoutInflater.from(parent.getContext()).inflate(viewId, parent, false);
        DialogListViewHolder holder = new DialogListViewHolder(view);
        holder.epcString = view.findViewById(resources.getIdentifier("epcString", "id", packageName));
        holder.context = view.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DialogListViewHolder holder, int position) {
        holder.epcString.setText(epcStringList.get(position));
        holder.context = this.context;
        Log.d("recycler", "onBindViewHolder: " + position);
    }

    @Override
    public int getItemCount() {
        return epcStringList.size();
    }

    public class DialogListViewHolder extends RecyclerView.ViewHolder {
        public Context context;
        public TextView epcString;

        public DialogListViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
