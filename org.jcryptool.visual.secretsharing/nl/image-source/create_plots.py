# -----BEGIN DISCLAIMER-----
# ******************************************************************************
# Copyright (c) 2011, 2021 JCrypTool Team and Contributors
#
# All rights reserved. This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# ******************************************************************************
# -----END DISCLAIMER-----

""" Script to create plots for the online help of Shamir's Secret Sharing plug-in.

Dependencies:
 * python3
 * numpy
 * matplotlib
 * (may require LaTex installation, see set_font_settings() method)

If placed in org.jcryptool.visual.secretsharing/nl/image-source and
ran via python3, it will automatically update the plots.
"""
import sys
from enum import Enum
from pathlib import Path

import matplotlib
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.figure import Figure


def interpolation_polynomial(x: np.ndarray) -> np.ndarray:
    y0 = 6
    y1 = 12
    y2 = 6
    x0 = 1
    x1 = 2
    x2 = 3
    return (((x - x1) / (x0 - x1)) * ((x - x2) / (x0 - x2)) * y0 + ((x - x0) / (x1 - x0)) * (
                (x - x2) / (x1 - x2)) * y1 + ((x - x0) / (x2 - x0)) * ((x - x1) / (x2 - x1)) * y2) % 17


def original_polynomial(x: np.ndarray) -> np.ndarray:
    return (5 + 7 * x + 11 * x ** 2) % 17


def original_polynomial_cont(x: np.ndarray) -> np.ndarray:
    return (5 + 7 * x + 11 * x ** 2)


class Language(Enum):
    ENGLISH = 0
    GERMAN = 1


desc_mapping = {
    (Language.ENGLISH, False): ["$P(x)$ if it were continuous", "Secret $s$", "Shares", "Other points"],
    (Language.ENGLISH, True): ["$P(x)$ if it were continuous", "$P'(x)$ if it were continuous", "Secret $s$", "Shares",
                               "Other points"],
    (Language.GERMAN, False): ["$P(x)$ wenn es kontinuierlich wäre", "Geheimnis $s$", "Shares", "Weitere Punkte"],
    (Language.GERMAN, True): ["$P(x)$ wenn es kontinuierlich wäre", "$P'(x)$ wenn es kontinuierlich wäre",
                              "Geheimnis $s$", "Shares", "Weitere Punkte"],
}


def plot(language: Language, show_interpolated: bool = False) -> Figure:
    x = np.arange(0, 7)
    x_cont = np.arange(0, 7, 0.1)
    y = original_polynomial(x)
    y_cont = original_polynomial(x_cont)
    # 0 == secret, 1 == share, 2 == normal polynomial point
    color_id = np.array([0, 1, 1, 1, 1, 2, 2])
    colors = {0: COLOR_SECRET, 1: COLOR_SHARES, 2: COLOR_OTHER}
    position_id = [(0.15, 0.05)] * len(color_id)
    assert (x.size == color_id.size)

    fig, ax = plt.subplots(figsize=(12, 8))
    slices = [0, 1, 6, x.size]
    bounds = [(i, j) for i, j in zip(slices[:-1], slices[1:])]

    for lower, upper in bounds:
        ax.scatter(x[lower:upper], y[lower:upper], c=[colors[id_] for id_ in color_id[lower:upper]], zorder=2,
                   s=100)
        for _x, _y, (x_offs, y_offs) in zip(x[lower:upper], y[lower:upper], position_id[lower:upper]):
            ax.annotate(f'$({_x}/{_y})$', (_x + x_offs, _y + y_offs), fontsize=14,
                        bbox=dict(boxstyle="round,pad=0.3", fc=COLOR_LABEL_BACKGROUND, ec="k", lw=0.72))

    ax.plot(x_cont, y_cont, zorder=1, color=COLOR_LINE, lw=1)
    if show_interpolated:
        x_lagrange = np.arange(0, 7, 0.1)
        y_lagrange = interpolation_polynomial(x_lagrange)
        ax.plot(x_lagrange, y_lagrange, zorder=1, color=COLOR_LINE_LAGRANGE, lw=1)

    ax.legend(desc_mapping[(language, show_interpolated)], fontsize=12)
    ax.spines['right'].set_visible(False)
    ax.spines['top'].set_visible(False)
    ax.axes.xaxis.set_visible(False)
    ax.axes.yaxis.set_visible(False)
    return fig


def run(target_dir_german: Path, target_dir_english) -> None:
    print(f"Creating and saving German plots to {target_dir_german}")
    plot(Language.GERMAN, False).savefig(Path(target_dir_german, prefix + 'fig1-example-plotted.svg'), bbox_inches='tight')
    plot(Language.GERMAN, True).savefig(Path(target_dir_german, prefix + 'fig2-example-plotted-with-interpolation.svg'), bbox_inches='tight')
    print(f"Creating and saving English plots to {target_dir_english}")
    plot(Language.ENGLISH, False).savefig(Path(target_dir_english, prefix + 'fig1-example-plotted.svg'), bbox_inches='tight')
    plot(Language.ENGLISH, True).savefig(Path(target_dir_english, prefix + 'fig2-example-plotted-with-interpolation.svg'), bbox_inches='tight')


def set_font_settings() -> None:
    matplotlib.rc('font', family='monospace')
    matplotlib.rc('font', monospace='Nimbus Mono PS')
    matplotlib.rc('text', usetex='true')


# Colors
COLOR_LABEL_BACKGROUND = "#F0F0F0"  # Gray
COLOR_SECRET = "#f0027f"  # Reddish pink
COLOR_SHARES = "#33a02c"  # Green
COLOR_OTHER = "#666666"  # Gray
COLOR_LINE = "#5fb7e5"  # Light blue
COLOR_LINE_LAGRANGE = "#947557"  # Light brown

prefix = 'sssAlgorithm_'
target_dir_de = Path('../de/help/content')
target_dir_en = Path('../en/help/content')

if __name__ == '__main__':

    set_font_settings()

    if len(sys.argv) == 3:
        run(Path(sys.argv[1]), Path(sys.argv[2]))
    elif target_dir_de.exists() and target_dir_en.exists():
        run(target_dir_de, target_dir_en)
    else:
        filename = Path(__file__)
        print('Script to create plots for the JCrypTool plug-in "Shamir\'s Secret Sharing"\n\n',
              f'Usage:\t{filename.name} german_directory english_directory\n\n',
              'Alternatively, you can place the script in the Shamir\'s Secret sharing plugin',
              'under "org.jcryptool.secretsharing/nl/image-source" to deploy automatically.')
