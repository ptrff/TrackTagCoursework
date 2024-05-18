package ru.ptrff.tracktag.data;

import java.util.Arrays;
import java.util.List;

import ru.ptrff.tracktag.R;
import ru.ptrff.tracktag.models.Option;

public class Options {
    public static final List<Option> more = Arrays.asList(
            new Option(OptionActions.SUBS, "Подписки", R.drawable.ic_subscription),
            new Option(OptionActions.PREF, "Настройки", R.drawable.ic_settings),
            new Option(OptionActions.ABOUT, "О программе", R.drawable.ic_about),
            new Option(OptionActions.AUTHOR, "Об авторе", R.drawable.ic_person)
    );

    public static final List<Option> home = Arrays.asList(
            new Option(OptionActions.SUBS, "Подписки", R.drawable.ic_subscription),
            new Option(OptionActions.SAVE, "Сохранить", R.drawable.ic_save),
            new Option(OptionActions.PREF, "Настройки", R.drawable.ic_settings),
            new Option(OptionActions.ABOUT, "О программе", R.drawable.ic_about),
            new Option(OptionActions.AUTHOR, "Об авторе", R.drawable.ic_person)
    );
}
