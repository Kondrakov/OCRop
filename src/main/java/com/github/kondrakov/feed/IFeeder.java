package com.github.kondrakov.feed;

import java.io.File;

public interface IFeeder {
    File feed(String input, String output);
}
