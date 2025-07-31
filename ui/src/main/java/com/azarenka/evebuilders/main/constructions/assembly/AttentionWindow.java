package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.vaadin.flow.component.html.Paragraph;

public class AttentionWindow extends CommonDialogComponent {

    public AttentionWindow() {
        super("attantion-window", false);
        setHeaderTitle("Attention");
        setWidth("500px");
        add(new Paragraph(getTranslation("message.attention")));
        getFooter().add(createCloseButton());
    }
}
