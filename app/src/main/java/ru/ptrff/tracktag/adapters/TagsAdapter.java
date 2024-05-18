package ru.ptrff.tracktag.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.ptrff.tracktag.R;
import ru.ptrff.tracktag.data.SearchFilter;
import ru.ptrff.tracktag.data.UserData;
import ru.ptrff.tracktag.databinding.ItemTagBinding;
import ru.ptrff.tracktag.models.Tag;

public class TagsAdapter extends ListAdapter<Tag, TagsAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private TagEvents tagEvents;
    private List<Tag> allTags;

    public interface TagEvents {
        void onLikeClick(Tag tag, boolean like);

        void onSubscribeClick(Tag tag);

        void onDeleteClick(Tag tag);

        void openTag(Tag tag);

        void focusOnTag(Tag tag);
    }

    public TagsAdapter(Context context) {
        super(new TagsDiffCallback());
        this.inflater = LayoutInflater.from(context);
    }

    public void setTagEvents(TagEvents tagEvents) {
        this.tagEvents = tagEvents;
    }

    public void setAllTags(List<Tag> list) {
        allTags = list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTagBinding binding = ItemTagBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tag tag = getItem(position);

        // background
        holder.binding.background.setOnClickListener(v -> {
            if (tagEvents != null) {
                tagEvents.openTag(tag);
            }
        });

        // Picture
        if (tag.getImage() != null && !tag.getImage().isEmpty()) {
            holder.binding.image.setVisibility(View.VISIBLE);
            Glide.with(holder.binding.image.getContext())
                    .load(tag.getImage())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.binding.image.setVisibility(View.GONE);
                            Log.d(this.getClass().getCanonicalName(), "No image: " + tag.getImage());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .transition(withCrossFade())
                    .into(holder.binding.image);
        } else {
            holder.binding.image.setVisibility(View.GONE);
        }

        // author
        if (tag.getUser() != null) {
            holder.binding.author.setText(tag.getUser().getUsername());
        } else {
            holder.binding.author.setText(R.string.guest);
        }

        // admin sub
        if (UserData.getInstance().getRole().equals("admin")) {
            holder.binding.admSubButton.setVisibility(View.VISIBLE);
            holder.binding.admSubButton.setCheckable(true);
            holder.binding.admSubButton.setIconResource(R.drawable.sl_notification);
            holder.binding.admSubButton.setChecked(UserData.getInstance().isSubscribed(tag.getUser()));
            holder.binding.admSubButton.setOnClickListener(v -> {
                if (tagEvents != null) {
                    tagEvents.onSubscribeClick(tag);
                }
            });
        } else {
            holder.binding.admSubButton.setVisibility(View.GONE);
        }

        // options
        if (tag.getUser() != null) {
            holder.binding.optionsButton.setVisibility(View.VISIBLE);
            if (UserData.getInstance().isLoggedIn() && (
                    UserData.getInstance().getUserName().equals(tag.getUser().getUsername()) ||
                            UserData.getInstance().getRole().equals("admin")
            )) {
                holder.binding.optionsButton.setCheckable(false);
                holder.binding.optionsButton.setIconResource(R.drawable.ic_delete);
                holder.binding.optionsButton.setOnClickListener(v -> {
                    if (tagEvents != null) {
                        tagEvents.onDeleteClick(tag);
                    }
                });
            } else {
                holder.binding.optionsButton.setCheckable(true);
                holder.binding.optionsButton.setIconResource(R.drawable.sl_notification);
                holder.binding.optionsButton.setChecked(UserData.getInstance().isSubscribed(tag.getUser()));
                holder.binding.optionsButton.setOnClickListener(v -> {
                    if (tagEvents != null) {
                        tagEvents.onSubscribeClick(tag);
                    }
                });
            }
        } else {
            holder.binding.optionsButton.setVisibility(View.GONE);
        }

        // description
        holder.binding.description.setText(tag.getDescription());

        // like
        if (tag.getLikes() == null) {
            tag.setLikes(new HashMap<>());
        }

        holder.binding.likeButton.clearOnCheckedChangeListeners();
        holder.binding.likeButton.setCheckable(UserData.getInstance().isLoggedIn());
        holder.binding.likeButton.setChecked(
                tag.getLikes().containsKey(UserData.getInstance().getUserId())
        );
        holder.binding.likeButton.setText("" + tag.getLikes().size());
        holder.binding.likeButton.addOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                tag.getLikes().put(UserData.getInstance().getUserId(), UserData.getInstance().getUserId());
                tagEvents.onLikeClick(tag, true);
            } else {
                tag.getLikes().remove(UserData.getInstance().getUserId());
                tagEvents.onLikeClick(tag, false);
            }
            holder.binding.likeButton.setText("" + tag.getLikes().size());
        });
        //focus
        holder.binding.focusButton.setOnClickListener(v -> {
            if (tagEvents != null) {
                tagEvents.focusOnTag(tag);
            }
        });
    }

    @SuppressLint("CheckResult")
    public void filter(CharSequence query) {
        SearchFilter f = SearchFilter.getInstance();
        if (allTags == null) return;
        Observable.fromIterable(allTags)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(tag -> {
                    if (f.getWithImage() != null && f.getWithImage() && !tag.getImage().isEmpty())
                        return false;
                    if (f.getWithoutImage() != null && f.getWithoutImage() && !tag.getImage().isEmpty())
                        return false;
                    if (f.getWithNoLikes() != null && f.getWithNoLikes() && !tag.getLikes().isEmpty())
                        return false;
                    return true;
                })
                .filter(tag -> {
                    if (query != null && !query.toString().isEmpty()) {
                        String filterPattern = query.toString().toLowerCase().trim();
                        if (f.getFilterBy() == null || f.getFilterBy() == 0) {
                            return tag.getUser() != null && tag.getUser().getUsername() != null &&
                                    tag.getUser().getUsername().toLowerCase().contains(filterPattern);
                        } else {
                            return tag.getDescription() != null && tag.getDescription().toLowerCase().contains(filterPattern);
                        }
                    }
                    return true;
                })
                .toSortedList((tag1, tag2) -> {
                    if (f.getSortBy() != null) {
                        switch (f.getSortBy()) {
                            case 2:
                                return Comparator.comparing(tag -> ((Tag) tag).getUser().getUsername()).compare(tag1, tag2);
                            case 3:
                                return Comparator.comparing(tag -> ((Tag) tag).getLikes().size()).reversed().compare(tag1, tag2);
                        }
                    }
                    return 0;
                })
                .map(filteredList -> {
                    if (f.getSortBy() != null && f.getSortBy() == 1) {
                        Collections.reverse(filteredList);
                    }
                    return filteredList;
                })
                .subscribe(this::submitList, throwable -> Log.e(this.getClass().getCanonicalName(), "Filter error", throwable));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTagBinding binding;

        public ViewHolder(@NonNull ItemTagBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class TagsDiffCallback extends DiffUtil.ItemCallback<Tag> {
        @Override
        public boolean areItemsTheSame(@NonNull Tag oldItem, @NonNull Tag newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Tag oldItem, @NonNull Tag newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
    }
}
