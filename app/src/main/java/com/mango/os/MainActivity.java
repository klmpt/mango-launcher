package com.mango.os;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class MainActivity extends Activity {

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private List<ResolveInfo> allApps;
    private List<ResolveInfo> filteredApps;
    private Set<String> pinnedPackages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        SharedPreferences prefs = getSharedPreferences("MangoPrefs", MODE_PRIVATE);
        pinnedPackages = new HashSet<>(Arrays.asList(prefs.getString("pinned", "").split(",")));

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        allApps = getPackageManager().queryIntentActivities(intent, 0);
        filteredApps = new ArrayList<>(allApps);

        
        recyclerView = findViewById(R.id.apps_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppAdapter();
        recyclerView.setAdapter(adapter);
        
        EditText searchBar = findViewById(R.id.search_bar);
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void afterTextChanged(Editable s) {}
            });
            searchBar.setOnLongClickListener(v -> {
                Toast.makeText(this, "🥭 Mango OS v1.0-Alpha \n by Wissy Studios (Kirill)", Toast.LENGTH_SHORT).show();
                return true;
            });
        }
        setupPinnedApps();
    }

    private void filter(String q) {
        filteredApps.clear();
        for (ResolveInfo app : allApps) {
            if (app.loadLabel(getPackageManager()).toString().toLowerCase().contains(q.toLowerCase()))
                filteredApps.add(app);
        }
        adapter.notifyDataSetChanged();
    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name; ImageView icon;
            ViewHolder(View v) { super(v); name = v.findViewById(R.id.app_name); icon = v.findViewById(R.id.app_icon); }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            ResolveInfo info = filteredApps.get(pos);
            holder.name.setText(info.loadLabel(getPackageManager()));
            holder.icon.setImageDrawable(info.loadIcon(getPackageManager()));
            holder.itemView.setOnClickListener(v -> startActivity(getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName)));
            holder.itemView.setOnLongClickListener(v -> {
                String pkg = info.activityInfo.packageName;
                if (pinnedPackages.contains(pkg)) pinnedPackages.remove(pkg); else pinnedPackages.add(pkg);
                getSharedPreferences("MangoPrefs", MODE_PRIVATE).edit().putString("pinned", String.join(",", pinnedPackages)).apply();
                setupPinnedApps();
                return true;
            });
        }
        public int getItemCount() { return filteredApps.size(); }
    }

    private void setupPinnedApps() {
            LinearLayout container = findViewById(R.id.pinned_apps_container);
            if (container == null) return;
            container.removeAllViews();
            
            for (String pkg : pinnedPackages) {
                if (pkg == null || pkg.isEmpty()) continue;
                
                Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
                if (intent != null) {
                    ImageView icon = new ImageView(this);
                    icon.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
                    icon.setPadding(10, 10, 10, 10);
                    
                    try {
                        
                        icon.setImageDrawable(getPackageManager().getApplicationIcon(pkg));
                        icon.setOnClickListener(v -> startActivity(intent));
                        container.addView(icon);
                    } catch (Exception e) {
                        
                    }
                }
            }
        } 
} 