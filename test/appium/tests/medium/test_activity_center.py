import pytest

from tests import marks, run_in_parallel
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.sign_in_view import SignInView
from views.chat_view import CommunityView


@pytest.mark.xdist_group(name="four_2")
@marks.medium
class TestActivityCenterMultipleDeviceMedium(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.home_1, self.home_2 = self.device_1.create_user(enable_notifications=True), self.device_2.create_user()
        self.public_key_user_1, self.username_1 = self.home_1.get_public_key_and_username(return_username=True)
        self.public_key_user_2, self.username_2 = self.home_2.get_public_key_and_username(return_username=True)
        [self.group_chat_name_1, self.group_chat_name_2] = "GroupChat1", "GroupChat2"

        self.message_from_sender = "Message sender"
        self.home_2.home_button.double_click()
        self.device_2_one_to_one_chat = self.home_2.add_contact(self.public_key_user_1)

    @marks.testrail_id(702183)
    def test_activity_center_reject_chats_no_pn(self):
        self.device_2.just_fyi('Device2 sends a message in 1-1 chat to Device1')
        self.device_2_one_to_one_chat.send_message(self.message_from_sender)

        self.device_1.just_fyi("Device 2: check there is no PN when receiving new message to activity centre")
        self.device_1.put_app_to_background()
        self.device_1.open_notification_bar()
        if self.home_1.element_by_text(self.message_from_sender).is_element_displayed():
            self.errors.append("Push notification with text was received for new message in activity centre")
        self.device_1.click_system_back_button(2)

        [home.home_button.double_click() for home in [self.home_1, self.home_2]]

        self.device_1.just_fyi('Device1 rejects chat and verifies it disappeared and not in Chats too')
        self.home_1.notifications_unread_badge.wait_and_click(20)
        self.home_1.notifications_select_button.click()
        self.home_1.element_by_text_part(self.username_2[:10]).click()
        self.home_1.element_by_text_part("Please add me to your contacts").click()
        self.home_1.notifications_reject_and_delete_button.click()
        if self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is on Activity Center view after action made on it")
        if self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is added on home after rejection")

        self.home_1.just_fyi("Verify there is still no chat after relogin")
        self.home_1.reopen_app()
        if self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat appears on Chats view after relogin")
        self.home_1.notifications_button.click()
        if self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat request reappears back in Activity Center view after relogin")

        self.errors.verify_no_errors()

    @marks.testrail_id(702184)
    def test_activity_center_accept_chats(self):
        [home.home_button.double_click() for home in [self.home_1, self.home_2]]

        self.device_2.just_fyi('Device2 sends a message in 1-1')
        self.home_2.get_chat_from_home_view(self.username_1).click()
        self.device_2_one_to_one_chat.send_message(self.message_from_sender)
        self.device_2_one_to_one_chat.home_button.double_click()

        self.device_1.just_fyi('Device1 accepts chat (via Select All button) and verifies it disappeared '
                               'from activity center view but present on Chats view')
        self.home_1.notifications_unread_badge.wait_and_click(20)
        self.home_1.notifications_select_button.click()
        self.home_1.notifications_select_all.click()
        self.home_1.notifications_accept_and_add_button.click()
        if self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat request stays on Activity Center view after it was accepted")

        self.home_1.home_button.double_click()
        if not self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is not added on home after accepted from Activity Center")

        self.errors.verify_no_errors()

    @marks.testrail_id(702187)
    def test_activity_center_accept_chats_only_from_contacts(self):
        [home.home_button.double_click() for home in [self.home_1, self.home_2]]

        if self.home_1.get_chat_from_home_view(self.username_2).is_element_displayed():
            self.home_1.delete_chat_long_press(self.username_2)

        self.device_1.just_fyi('Device1 sets permissions to accept chat requests only from trusted contacts')
        profile_1 = self.home_1.profile_button.click()
        profile_1.privacy_and_security_button.click()
        profile_1.accept_new_chats_from.click()
        profile_1.accept_new_chats_from_contacts_only.click()
        profile_1.profile_button.click()

        self.device_1.just_fyi('Device2 creates 1-1 chat')
        self.home_2.home_button.double_click()
        self.home_2.get_chat(self.username_1).click()
        self.device_2_one_to_one_chat.send_message(self.message_from_sender)
        self.device_2_one_to_one_chat.home_button.double_click()

        self.device_1.just_fyi('Device1 check there are no any chats in Activity Center nor Chats view')
        self.home_1.home_button.double_click()

        if self.home_1.element_by_text_part(self.username_2).is_element_displayed():
            self.errors.append("Chats are present on Chats view despite they created by non-contact")
        self.home_1.notifications_button.click()

        if self.home_1.element_by_text_part(self.username_2).is_element_displayed():
            self.errors.append("Chat is present in Activity Center view despite they created by non-contact")

        self.device_1.just_fyi('Device1 adds Device2 in Contacts so chat requests should be visible now')
        self.home_1.home_button.double_click()
        self.home_1.add_contact(self.public_key_user_2)

        self.device_1.just_fyi('Device2 creates 1-1 chat Group chats once again')
        self.home_2.home_button.double_click()
        self.home_2.get_chat_from_home_view(self.username_1).click()
        self.device_2_one_to_one_chat.send_message(self.message_from_sender)
        self.device_2_one_to_one_chat.home_button.double_click()
        self.home_2.create_group_chat([self.username_1], group_chat_name=self.group_chat_name_2)

        self.device_1.just_fyi('Device1 verifies 1-1 chat Group chats are visible')
        self.home_1.home_button.double_click()
        if not self.home_1.element_by_text_part(
                self.username_2).is_element_displayed() or not self.home_1.element_by_text_part(
            self.group_chat_name_2).is_element_displayed():
            self.errors.append("Chats are not present on Chats view while they have to!")

        self.errors.verify_no_errors()

    @marks.testrail_id(702185)
    def test_activity_center_notifications_on_mentions_in_groups_and_empty_state(self):
        [home.home_button.double_click() for home in [self.home_1, self.home_2]]

        if not self.home_1.element_by_text_part(self.username_2).is_element_displayed():
            self.home_1.handle_contact_request(self.username_2)
            self.home_1.home_button.double_click()

        self.device_2.just_fyi('Device2 creates Group chat 3')
        self.home_2.create_group_chat([self.username_1], group_chat_name=self.group_chat_name_1)
        self.home_2.home_button.double_click()

        self.home_1.just_fyi("Device1 joins Group chat 3")
        group_chat_1 = self.home_1.get_chat(self.group_chat_name_1).click()
        group_chat_1.join_chat_button.click_if_shown()
        group_chat_1.home_button.double_click()

        self.home_2.just_fyi("Device2 mentions Device1 in Group chat 3")
        chat_2 = self.home_2.get_chat_from_home_view(self.group_chat_name_1).click()
        chat_2.select_mention_from_suggestion_list(self.username_1, self.username_1[:2])
        chat_2.send_as_keyevent("group")
        group_chat_message = self.username_1 + " group"
        chat_2.send_message_button.click()

        self.home_1.just_fyi("Device1 checks unread indicator on Activity center bell")
        if not self.home_1.notifications_unread_badge.is_element_displayed():
            self.errors.append("Unread badge is NOT shown after receiving mentions from Group")
        self.home_1.notifications_unread_badge.click_until_absense_of_element(self.home_1.plus_button, 6)

        self.home_1.just_fyi("Check that notification from group is presented in Activity Center")
        if not self.home_1.get_chat_from_activity_center_view(
                self.username_2).chat_message_preview == group_chat_message:
            self.errors.append("No mention in Activity Center for Group Chat")

        self.home_1.just_fyi("Open group chat where user mentioned")
        self.home_1.get_chat_from_activity_center_view(self.username_2).click()
        self.home_1.home_button.double_click()

        self.home_1.just_fyi("Check there are no unread messages counter on chats after message is read")
        if (self.home_1.notifications_unread_badge.is_element_displayed() or
                self.home_1.get_chat_from_home_view(self.group_chat_name_1).new_messages_counter.text == "1"):
            self.errors.append("Unread message indicator is kept after message is read in chat")

        self.home_1.just_fyi("Check there is an empty view on Activity Center")
        self.home_1.notifications_button.click()
        if not self.home_1.element_by_translation_id('empty-activity-center').is_element_displayed():
            self.errors.append("Activity Center still has some chats after user has opened all of them")

        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="two_2")
@marks.new_ui_critical
class TestActivityCenterMultipleDevicePR(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user,), (self.device_2.create_user,))))
        self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.profile_1, self.profile_2 = self.home_1.get_profile_view(), self.home_2.get_profile_view()
        users = self.loop.run_until_complete(run_in_parallel(
            ((self.home_1.get_public_key_and_username, True),
             (self.home_2.get_public_key_and_username, True))
        ))
        self.public_key_1, self.default_username_1 = users[0]
        self.public_key_2, self.default_username_2 = users[1]

        self.profile_1.just_fyi("Enabling PNs")
        self.profile_1.switch_push_notifications()

    @marks.testrail_id(702850)
    def test_activity_center_decline_contact_request_no_pn(self):
        self.device_1.put_app_to_background()
        self.device_2.just_fyi('Device2 sends a contact request to Device1')
        self.profile_2.click_system_back_button_until_element_is_shown(self.profile_2.contacts_button)
        self.profile_2.add_contact_via_contacts_list(self.public_key_1)
        self.profile_2.click_system_back_button_until_element_is_shown(self.profile_2.contacts_button)

        self.device_1.just_fyi("Device 2: check there is no PN when receiving new message to activity centre")
        self.device_1.open_notification_bar()
        if self.home_1.element_by_text_part("Please add me to your contacts").is_element_displayed():
            self.errors.append("Push notification with text was received for new message in activity centre")
        self.device_1.click_system_back_button(2)

        [home.chats_tab.double_click() for home in [self.home_1, self.home_2]]

        self.device_1.just_fyi('Device1 verifies pending contact request')
        self.home_1.contacts_tab.click()
        for indicator in (self.home_1.notifications_unread_badge, self.home_1.contact_new_badge):
            if not indicator.is_element_displayed():
                self.errors.append("Unread indicator on contacts tab or on activity center is not shown!")
        if self.home_1.pending_contact_request_text.text != '1':
            self.errors.append("The amount of contact requests is not shown!")

        self.device_1.just_fyi('Device1 declines pending contact request')
        self.home_1.handle_contact_request(username=self.default_username_2, accept=False)
        for indicator in (self.home_1.notifications_unread_badge, self.home_1.contact_new_badge, self.home_1.pending_contact_request_text):
            if indicator.is_element_displayed():
                self.errors.append("Unread indicator on contacts tab or on activity center is shown after declining contact request!")

        self.errors.verify_no_errors()

    @marks.testrail_id(702851)
    @marks.xfail(reason='blocked by #14798')
    def test_activity_center_mentions_in_community_jump_to(self):
        self.device_2.just_fyi('Device2 sends a contact request to Device1')
        self.home_2.browser_tab.click()
        self.profile_2.click_system_back_button_until_element_is_shown(self.profile_2.contacts_button)
        self.profile_2.add_contact_via_contacts_list(self.public_key_1)
        self.profile_2.click_system_back_button_until_element_is_shown(self.profile_2.contacts_button)

        self.device_1.just_fyi('Device1 accepts pending contact request and check contact list')
        self.home_1.chats_tab.click()
        self.home_1.handle_contact_request(username=self.default_username_2)
        self.home_1.contacts_tab.click()
        if not self.home_1.contact_details(username=self.default_username_2).is_element_displayed(20):
            self.errors.append("Contact was not added to contact list after accepting contact request")
        self.home_1.recent_tab.click()

        self.device_1.just_fyi('Creating and join community from Device1 and Device2')
        self.text_message = 'first message in community'
        self.community_name = self.home_1.get_random_chat_name()
        self.channel_name = self.home_1.get_random_chat_name()
        self.home_1.communities_tab.click()
        self.home_1.create_community(name=self.community_name, description='community to test', require_approval=False)
        self.community_1 = CommunityView(self.drivers[0])
        self.community_1.send_invite_to_community(self.default_username_2)
        self.channel_1 = self.community_1.add_channel(self.channel_name)
        self.channel_1.send_message(self.text_message)

        self.home_2.chats_tab.click()
        self.home_2.recent_tab.click()
        self.chat_2 = self.home_2.get_chat(self.default_username_1).click()
        self.chat_2.element_by_text_part('View').click()
        self.community_2 = CommunityView(self.drivers[1])
        self.community_2.join_button.click()
        self.channel_2 = self.community_2.get_chat(self.channel_name).click()

        self.community_2.just_fyi("Check Jump to screen and redirect on tap")
        self.channel_2.click_system_back_button()
        self.community_2.jump_to_button.click()
        for card in (self.community_name, self.default_username_1):
            if not self.community_2.element_by_text_part(card).is_element_displayed(20):
                self.errors.append("Card %s is not shown on Jump to screen!" % card)
        self.community_2.element_by_translation_id("community-channel").click()
        if not self.channel_2.chat_element_by_text(self.text_message).is_element_displayed(20):
            self.errors.append("User was not redirected to community channel after tappin on community channel card!")
        self.channel_2.click_system_back_button()
        self.community_2.jump_to_button.click()
        self.community_2.element_by_text_part(self.default_username_1).click()
        if not self.chat_2.element_by_text_part('View').is_element_displayed(20):
            self.errors.append("User was not redirected to 1-1 chat after tapping card!")

        # Blocked because of 14648
        # self.device_2.just_fyi('Mention user1 and check activity centre')
        # self.channel_2.select_mention_from_suggestion_list(self.default_username_1, self.default_username_1[:2])
        # self.channel_2.send_as_keyevent("mention activity centre")
        # ac_chat_message = self.default_username_1 + " mention activity centre"
        # self.channel_2.send_message_button.click()
        # self.home_1.click_system_back_button_until_element_is_shown()
        # self.home_1.open_activity_center_button.click()
        # for text in ('Mention', '@%s' % ac_chat_message):
        #     if self.home_1.element_by_text(text).is_element_displayed(30):
        #         self.errors.append("Mention is not shown in activity centre!")
        # self.home_1.close_activity_centre.click()
        #
        # self.device_2.just_fyi('Mention user1 and check PN')
        # self.device_1.put_app_to_background()
        # self.channel_2.select_mention_from_suggestion_list(self.default_username_1, self.default_username_1[:2])
        # self.channel_2.send_as_keyevent("group")
        # group_chat_message = self.default_username_1 + " group"
        # self.channel_2.send_message_button.click()
        # self.device_1.open_notification_bar()
        # if self.home_1.get_pn(group_chat_message):
        #     self.home_1.get_pn(group_chat_message).click()
        # else:
        #     self.home_1.driver.fail("No PN for mention in community!")
        # if not self.channel_1.chat_element_by_text(group_chat_message).is_element_displayed(20):
        #     self.errors.append("No redirect to channel after tap on PN with mention!")

        self.errors.verify_no_errors()
