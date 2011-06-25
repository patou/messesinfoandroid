package cef.messesinfo.maps;

import android.graphics.drawable.Drawable;
import de.android1.overlaymanager.OverlayManager;

public abstract class ChurchItemizedOverlay extends BalloonItemizedOverlay {
    public ChurchItemizedOverlay(OverlayManager manager, String name, Drawable drawable) {
	super(manager, name, drawable);
    }
}
