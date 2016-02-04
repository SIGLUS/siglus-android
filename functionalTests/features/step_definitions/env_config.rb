module EnvConfig

    def self.getConfig()

        dev_env=true

        if dev_env
            {
                username:"superuser",
                password:"password1",
            }
        else
            {
                username:"test_user",
                password:"testuser123",
            }
        end
    end
end
