<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!-- AppBarLayout with Toolbar -->
    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:theme="@style/Theme.PocketBrain.AppBarOverlay">

        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="#240E3C"
            app:popupTheme="@style/Theme.PocketBrain.PopupOverlay"
            app:title="Statistics"
            app:titleTextColor="#FFFFFF" />

    </com.google.android.material.appbar.AppBarLayout>

    <!-- ScrollView for the rest of the content -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- Month Selector -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginBottom="16dp">

                <ImageButton
                    android:id="@+id/btnPrevMonth"
                    android:layout_width="48dp"
                    android:layout_height="48dp"
                    android:src="@drawable/ic_arrow_left"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:contentDescription="Previous Month" />

                <TextView
                    android:id="@+id/textCurrentMonth"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="April 2025"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:gravity="center" />

                <ImageButton
                    android:id="@+id/btnNextMonth"
                    android:layout_width="48dp"
                    android:layout_height="48dp"
                    android:src="@drawable/ic_arrow_right"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:contentDescription="Next Month" />
            </LinearLayout>

            <!-- Expenses By Category Title -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Expenses By Category"
                android:textSize="18sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp" />

            <!-- Pie Chart -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="240dp"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="8dp"
                app:cardElevation="4dp">

                <com.github.mikephil.charting.charts.PieChart
                    android:id="@+id/pieChart"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:padding="8dp" />

                <TextView
                    android:id="@+id/textNoDataForChart"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:text="No expense data for this month"
                    android:gravity="center"
                    android:textSize="16sp"
                    android:visibility="gone" />
            </androidx.cardview.widget.CardView>

            <!-- Category List Title -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Expense Categories"
                android:textSize="18sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp" />

            <!-- Category List -->
            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/recyclerCategories"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:nestedScrollingEnabled="false" />

            <TextView
                android:id="@+id/textNoCategories"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="No category data for this month"
                android:gravity="center"
                android:padding="16dp"
                android:visibility="gone" />

        </LinearLayout>
    </ScrollView>
</LinearLayout>