/*
 * ToolbarsOptionsPanel.java
 *
 * Copyright (c) 2010 Flaviano Petrocchi <flavianopetrocchi at gmail.com>.
 * All rights reserved.
 *
 * This file is part of JPdfBookmarks.
 *
 * JPdfBookmarks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPdfBookmarks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPdfBookmarks.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.flavianopetrocchi.jpdfbookmarks;

import it.flavianopetrocchi.utilities.Ut;

/**
 * The toolbar options panel of the options dialog.
 *
 * @author fla
 */
public class ToolbarsOptionsPanel extends javax.swing.JPanel {

    private Prefs userPrefs;

    /**
     * Creates new form ToolbarsOptionsPanel and sets the current status of the
     * checkboxes there.
     */
    public ToolbarsOptionsPanel(Prefs userPrefs) {
        this.userPrefs = userPrefs;
        initComponents();
        checkAddBookmark.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_ADD_TB));
        checkApplyDestination.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_SETDEST_TB));
        checkEditBookmark.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_CHANGE_TB));
        checkEditStyle.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_STYLE_TB));
        checkFileActions.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_FILE_TB));
        checkFitType.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_FITTYPE_TB));
        checkNavigation.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_NAVIGATION_TB));
        checkOtherActions.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_OTHERS_TB));
        checkUndoRedo.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_UNDO_TB));
        checkWebOperations.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_WEB_TB));
        checkZoom.setSelected(userPrefs.getShowToolbar(Prefs.SHOW_ZOOM_TB));
    }

    public void saveToolbarPreferences() {
        userPrefs.setShowToolbar(Prefs.SHOW_ADD_TB, checkAddBookmark.isSelected());
        userPrefs.setShowToolbar(Prefs.SHOW_SETDEST_TB, checkApplyDestination.isSelected());
        userPrefs.setShowToolbar(Prefs.SHOW_CHANGE_TB, checkEditBookmark.isSelected());
        userPrefs.setShowToolbar(Prefs.SHOW_STYLE_TB, checkEditStyle.isSelected());
        userPrefs.setShowToolbar(Prefs.SHOW_FILE_TB, checkFileActions.isSelected());
        userPrefs.setShowToolbar(Prefs.SHOW_FITTYPE_TB, checkFitType.isSelected());
        userPrefs.setShowToolbar(Prefs.SHOW_NAVIGATION_TB, checkNavigation.isSelected());
        userPrefs.setShowToolbar(Prefs.SHOW_OTHERS_TB, checkOtherActions.isSelected());
        userPrefs.setShowToolbar(Prefs.SHOW_UNDO_TB, checkUndoRedo.isSelected());
        userPrefs.setShowToolbar(Prefs.SHOW_WEB_TB, checkWebOperations.isSelected());
        userPrefs.setShowToolbar(Prefs.SHOW_ZOOM_TB, checkZoom.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMainToolbars = new javax.swing.JPanel();
        checkFileActions = new javax.swing.JCheckBox();
        checkZoom = new javax.swing.JCheckBox();
        checkOtherActions = new javax.swing.JCheckBox();
        checkFitType = new javax.swing.JCheckBox();
        checkNavigation = new javax.swing.JCheckBox();
        checkWebOperations = new javax.swing.JCheckBox();
        btnShowAllMain = new javax.swing.JButton();
        btnHideAllMain = new javax.swing.JButton();
        panelBookmarksToolbars = new javax.swing.JPanel();
        checkAddBookmark = new javax.swing.JCheckBox();
        checkEditBookmark = new javax.swing.JCheckBox();
        checkUndoRedo = new javax.swing.JCheckBox();
        checkEditStyle = new javax.swing.JCheckBox();
        checkApplyDestination = new javax.swing.JCheckBox();
        btnShowAllBookmarks = new javax.swing.JButton();
        btnHideAllBookmarks = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("it/flavianopetrocchi/jpdfbookmarks/locales/localizedText"); // NOI18N
        panelMainToolbars.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MENU_SHOW_MAIN_TOOLBARS"))); // NOI18N

        checkFileActions.setText(bundle.getString("TOOLBAR_FILE")); // NOI18N

        checkZoom.setText(bundle.getString("TOOLBAR_ZOOM")); // NOI18N

        checkOtherActions.setText(bundle.getString("TOOLBAR_OTHERS")); // NOI18N

        checkFitType.setText(bundle.getString("TOOLBAR_FITTYPE")); // NOI18N

        checkNavigation.setText(bundle.getString("TOOLBAR_NAV")); // NOI18N

        checkWebOperations.setText(bundle.getString("TOOLBAR_WEB")); // NOI18N

        btnShowAllMain.setText(bundle.getString("TOOLBAR_SHOW_ALL")); // NOI18N
        btnShowAllMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowAllMainActionPerformed(evt);
            }
        });

        btnHideAllMain.setText(bundle.getString("TOOLBAR_HIDE_ALL")); // NOI18N
        btnHideAllMain.setMaximumSize(new java.awt.Dimension(65, 29));
        btnHideAllMain.setMinimumSize(new java.awt.Dimension(65, 29));
        btnHideAllMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHideAllMainActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMainToolbarsLayout = new javax.swing.GroupLayout(panelMainToolbars);
        panelMainToolbars.setLayout(panelMainToolbarsLayout);
        panelMainToolbarsLayout.setHorizontalGroup(
            panelMainToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainToolbarsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkFileActions)
                    .addComponent(checkNavigation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkOtherActions)
                    .addComponent(checkFitType))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkZoom)
                    .addComponent(checkWebOperations))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnHideAllMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnShowAllMain, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                .addGap(64, 64, 64))
        );
        panelMainToolbarsLayout.setVerticalGroup(
            panelMainToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainToolbarsLayout.createSequentialGroup()
                .addGroup(panelMainToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkFileActions)
                    .addComponent(checkZoom)
                    .addComponent(checkFitType)
                    .addComponent(btnShowAllMain))
                .addGap(2, 2, 2)
                .addGroup(panelMainToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkNavigation)
                    .addComponent(checkOtherActions)
                    .addComponent(checkWebOperations)
                    .addComponent(btnHideAllMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelBookmarksToolbars.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MENU_SHOW_BOOKMARKS_TOOLBARS"))); // NOI18N

        checkAddBookmark.setText(bundle.getString("TOOLBAR_ADD")); // NOI18N

        checkEditBookmark.setText(bundle.getString("TOOLBAR_CHANGE")); // NOI18N

        checkUndoRedo.setText(bundle.getString("TOOLBAR_UNDO")); // NOI18N

        checkEditStyle.setText(bundle.getString("TOOLBAR_STYLE")); // NOI18N

        checkApplyDestination.setText(bundle.getString("TOOLBAR_SETDEST")); // NOI18N

        btnShowAllBookmarks.setText(bundle.getString("TOOLBAR_SHOW_ALL")); // NOI18N
        btnShowAllBookmarks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowAllBookmarksActionPerformed(evt);
            }
        });

        btnHideAllBookmarks.setText(bundle.getString("TOOLBAR_HIDE_ALL")); // NOI18N
        btnHideAllBookmarks.setMaximumSize(new java.awt.Dimension(65, 29));
        btnHideAllBookmarks.setMinimumSize(new java.awt.Dimension(65, 29));
        btnHideAllBookmarks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHideAllBookmarksActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBookmarksToolbarsLayout = new javax.swing.GroupLayout(panelBookmarksToolbars);
        panelBookmarksToolbars.setLayout(panelBookmarksToolbarsLayout);
        panelBookmarksToolbarsLayout.setHorizontalGroup(
            panelBookmarksToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBookmarksToolbarsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBookmarksToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(checkAddBookmark, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(checkEditStyle, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBookmarksToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBookmarksToolbarsLayout.createSequentialGroup()
                        .addComponent(checkEditBookmark)
                        .addGap(18, 18, 18)
                        .addComponent(checkUndoRedo))
                    .addComponent(checkApplyDestination))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBookmarksToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnHideAllBookmarks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnShowAllBookmarks, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
                .addGap(65, 65, 65))
        );
        panelBookmarksToolbarsLayout.setVerticalGroup(
            panelBookmarksToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBookmarksToolbarsLayout.createSequentialGroup()
                .addGroup(panelBookmarksToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkAddBookmark)
                    .addComponent(checkEditBookmark)
                    .addComponent(checkUndoRedo)
                    .addComponent(btnShowAllBookmarks))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBookmarksToolbarsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkEditStyle)
                    .addComponent(checkApplyDestination)
                    .addComponent(btnHideAllBookmarks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelMainToolbars, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                    .addComponent(panelBookmarksToolbars, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMainToolbars, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBookmarksToolbars, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnShowAllMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllMainActionPerformed
        Ut.setSelectedButtons(true, checkFileActions, checkFitType, checkZoom,
                checkNavigation, checkOtherActions, checkWebOperations);
    }//GEN-LAST:event_btnShowAllMainActionPerformed

    private void btnHideAllMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHideAllMainActionPerformed
        Ut.setSelectedButtons(false, checkFileActions, checkFitType, checkZoom,
                checkNavigation, checkOtherActions, checkWebOperations);
    }//GEN-LAST:event_btnHideAllMainActionPerformed

    private void btnShowAllBookmarksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllBookmarksActionPerformed
        Ut.setSelectedButtons(true, checkAddBookmark, checkEditBookmark,
                checkUndoRedo, checkEditStyle, checkApplyDestination);
    }//GEN-LAST:event_btnShowAllBookmarksActionPerformed

    private void btnHideAllBookmarksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHideAllBookmarksActionPerformed
        Ut.setSelectedButtons(false, checkAddBookmark, checkEditBookmark,
                checkUndoRedo, checkEditStyle, checkApplyDestination);        // TODO add your handling code here:
    }//GEN-LAST:event_btnHideAllBookmarksActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHideAllBookmarks;
    private javax.swing.JButton btnHideAllMain;
    private javax.swing.JButton btnShowAllBookmarks;
    private javax.swing.JButton btnShowAllMain;
    private javax.swing.JCheckBox checkAddBookmark;
    private javax.swing.JCheckBox checkApplyDestination;
    private javax.swing.JCheckBox checkEditBookmark;
    private javax.swing.JCheckBox checkEditStyle;
    private javax.swing.JCheckBox checkFileActions;
    private javax.swing.JCheckBox checkFitType;
    private javax.swing.JCheckBox checkNavigation;
    private javax.swing.JCheckBox checkOtherActions;
    private javax.swing.JCheckBox checkUndoRedo;
    private javax.swing.JCheckBox checkWebOperations;
    private javax.swing.JCheckBox checkZoom;
    private javax.swing.JPanel panelBookmarksToolbars;
    private javax.swing.JPanel panelMainToolbars;
    // End of variables declaration//GEN-END:variables
}
